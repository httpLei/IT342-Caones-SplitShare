package edu.cit.caones.splitshare.service;

import edu.cit.caones.splitshare.dto.request.CreateGroupRequest;
import edu.cit.caones.splitshare.dto.request.UpdateGroupRequest;
import edu.cit.caones.splitshare.dto.response.ExpenseDto;
import edu.cit.caones.splitshare.dto.response.GroupDetailsDto;
import edu.cit.caones.splitshare.dto.response.GroupMemberBalanceDto;
import edu.cit.caones.splitshare.dto.response.GroupSummaryDto;
import edu.cit.caones.splitshare.entity.BalanceSettlement;
import edu.cit.caones.splitshare.entity.Expense;
import edu.cit.caones.splitshare.entity.Group;
import edu.cit.caones.splitshare.entity.GroupMember;
import edu.cit.caones.splitshare.entity.User;
import edu.cit.caones.splitshare.repository.BalanceSettlementRepository;
import edu.cit.caones.splitshare.repository.ExpenseRepository;
import edu.cit.caones.splitshare.repository.GroupRepository;
import edu.cit.caones.splitshare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);

    private final GroupRepository groupRepository;
    private final ExpenseRepository expenseRepository;
    private final BalanceSettlementRepository balanceSettlementRepository;
    private final UserRepository userRepository;
    private final UserSocialService userSocialService;

    @Transactional
    public GroupSummaryDto createGroup(CreateGroupRequest request, String creatorEmail) {
        User creator = getUserByEmail(creatorEmail);

        Set<String> memberEmails = normalizeEmails(request.getMemberEmails());

        Group group = Group.builder()
                .name(request.getName().trim())
                .createdBy(creator)
                .build();

        group.addMember(createGroupMember(creator));

        for (String email : memberEmails) {
            if (email.equalsIgnoreCase(creator.getEmail())) {
                continue;
            }

            if (!userSocialService.isMutualByEmails(creator.getEmail(), email)) {
                throw new IllegalArgumentException("You can only add users with a mutual follow relationship: " + email);
            }

            User member = getUserByEmail(email);
            group.addMember(createGroupMember(member));
        }

        Group saved = groupRepository.save(group);
        return toSummaryDto(saved, creatorEmail);
    }

    @Transactional
    public GroupDetailsDto updateGroup(Long groupId, UpdateGroupRequest request, String currentUserEmail) {
        Group group = getAccessibleGroupForUpdate(groupId, currentUserEmail);
        boolean changed = false;

        if (request.getName() != null && !request.getName().trim().isBlank()) {
            group.setName(request.getName().trim());
            changed = true;
        }

        Set<String> memberEmails = normalizeEmails(request.getMemberEmails());
        Set<String> existingMembers = group.getMembers().stream()
                .map(member -> member.getUser().getEmail().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        for (String email : memberEmails) {
            if (email.equalsIgnoreCase(currentUserEmail) || existingMembers.contains(email)) {
                continue;
            }

            if (!userSocialService.isMutualByEmails(currentUserEmail, email)) {
                throw new IllegalArgumentException("You can only add users with a mutual follow relationship: " + email);
            }

            User member = getUserByEmail(email);
            group.addMember(createGroupMember(member));
            changed = true;
        }

        if (!changed) {
            throw new IllegalArgumentException("No updates were provided");
        }

        Group saved = groupRepository.save(group);
        return toDetailsDto(saved, currentUserEmail);
    }

    @Transactional(readOnly = true)
    public List<GroupSummaryDto> getMyGroups(String currentUserEmail) {
        return groupRepository.findAllVisibleToUser(currentUserEmail)
                .stream()
                .map(group -> toSummaryDto(group, currentUserEmail))
                .toList();
    }

    @Transactional(readOnly = true)
    public GroupDetailsDto getGroupDetails(Long groupId, String currentUserEmail) {
        Group group = getAccessibleGroup(groupId, currentUserEmail);
        return toDetailsDto(group, currentUserEmail);
    }

    @Transactional
    public GroupDetailsDto confirmSettlement(Long groupId, String counterpartEmail, String currentUserEmail) {
        Group group = getAccessibleGroup(groupId, currentUserEmail);

        User currentUser = getUserByEmail(currentUserEmail);
        User counterpart = getUserByEmail(counterpartEmail);

        if (currentUser.getEmail().equalsIgnoreCase(counterpart.getEmail())) {
            throw new IllegalArgumentException("You cannot settle with yourself");
        }

        ensureMember(group, counterpart.getEmail());

        List<Expense> expenses = expenseRepository.findByGroup_IdOrderByCreatedAtDesc(group.getId());
        Map<String, BigDecimal> pairwiseBalances = calculatePairwiseBalances(group, expenses, currentUserEmail);
        BigDecimal outstanding = pairwiseBalances.getOrDefault(counterpart.getEmail().toLowerCase(Locale.ROOT), BigDecimal.ZERO)
                .abs()
                .setScale(2, RoundingMode.HALF_UP);

        if (outstanding.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("No outstanding balance to settle with this member");
        }

        User userA = currentUser.getId() <= counterpart.getId() ? currentUser : counterpart;
        User userB = currentUser.getId() <= counterpart.getId() ? counterpart : currentUser;

        BalanceSettlement settlement = balanceSettlementRepository
                .findFirstByGroup_IdAndUserA_IdAndUserB_IdAndSettledAtIsNull(group.getId(), userA.getId(), userB.getId())
                .orElseGet(() -> BalanceSettlement.builder()
                        .group(group)
                        .userA(userA)
                        .userB(userB)
                        .amount(outstanding)
                        .build());

        boolean currentIsUserA = settlement.getUserA().getId().equals(currentUser.getId());
        if (currentIsUserA) {
            settlement.setConfirmedByUserA(true);
        } else {
            settlement.setConfirmedByUserB(true);
        }

        if (settlement.isConfirmedByUserA() && settlement.isConfirmedByUserB()) {
            settlement.setSettledAt(Instant.now());
        }

        balanceSettlementRepository.save(settlement);

        return toDetailsDto(group, currentUserEmail);
    }

    @Transactional(readOnly = true)
    public Group getAccessibleGroup(Long groupId, String currentUserEmail) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("Group not found"));
        ensureMember(group, currentUserEmail);
        return group;
    }

    public Group getAccessibleGroupForUpdate(Long groupId, String currentUserEmail) {
        return getAccessibleGroup(groupId, currentUserEmail);
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + email));
    }

    private void ensureMember(Group group, String email) {
        boolean member = group.getMembers().stream()
                .anyMatch(groupMember -> groupMember.getUser().getEmail().equalsIgnoreCase(email));
        if (!member) {
            throw new AccessDeniedException("You are not a member of this group");
        }
    }

    private GroupMember createGroupMember(User user) {
        return GroupMember.builder()
                .user(user)
                .build();
    }

    private GroupSummaryDto toSummaryDto(Group group, String currentUserEmail) {
        BigDecimal total = sumExpenses(group.getExpenses());
        BigDecimal myBalance = calculateBalanceForUser(group, currentUserEmail);

        return GroupSummaryDto.builder()
                .id(group.getId())
                .name(group.getName())
                .members(group.getMembers().stream()
                        .map(member -> member.getUser().getFirstname() + " " + member.getUser().getLastname())
                        .toList())
                .total(total)
                .balance(myBalance)
                .createdAt(group.getCreatedAt())
                .build();
    }

    private GroupDetailsDto toDetailsDto(Group group, String currentUserEmail) {
        List<Expense> expenses = expenseRepository.findByGroup_IdOrderByCreatedAtDesc(group.getId());
        BigDecimal total = sumExpenses(expenses);
        Map<String, BigDecimal> balances = calculatePairwiseBalances(group, expenses, currentUserEmail);
        Map<String, BigDecimal> settledByCounterpart = getSettledAmountsByCounterpart(group.getId(), currentUserEmail);
        Map<String, BalanceSettlement> pendingByCounterpart = getPendingSettlementsByCounterpart(group.getId(), currentUserEmail);
        applySettlementsToPairwiseBalances(balances, settledByCounterpart);

        BigDecimal netBalance = balances.entrySet().stream()
            .filter(e -> !e.getKey().equalsIgnoreCase(currentUserEmail))
            .map(Map.Entry::getValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        return GroupDetailsDto.builder()
                .id(group.getId())
                .name(group.getName())
                .members(group.getMembers().stream()
                        .map(member -> member.getUser().getFirstname() + " " + member.getUser().getLastname())
                        .toList())
                .memberEmails(group.getMembers().stream()
                    .map(member -> member.getUser().getEmail())
                    .toList())
                .total(total)
                .balance(netBalance)
                .createdAt(group.getCreatedAt())
                .balances(group.getMembers().stream()
                        .map(member -> {
                            String email = member.getUser().getEmail().toLowerCase(Locale.ROOT);
                            BigDecimal amount = balances.getOrDefault(email, BigDecimal.ZERO);
                            BalanceSettlement pending = pendingByCounterpart.get(email);
                            return GroupMemberBalanceDto.builder()
                                    .name(member.getUser().getFirstname() + " " + member.getUser().getLastname())
                                    .email(member.getUser().getEmail())
                                    .initial(buildInitials(member.getUser().getFirstname(), member.getUser().getLastname()))
                                    .amount(amount.abs().setScale(2, RoundingMode.HALF_UP))
                                    .positive(amount.compareTo(BigDecimal.ZERO) >= 0)
                                .settlementPending(pending != null)
                                .settledByCurrentUser(isConfirmedByCurrentUser(pending, currentUserEmail))
                                    .build();
                        })
                        .toList())
                .expenses(expenses.stream()
                        .map(expense -> toExpenseDto(expense, balances, currentUserEmail))
                        .toList())
                .build();
    }

    private Set<String> normalizeEmails(List<String> emails) {
        if (emails == null) {
            return Set.of();
        }

        return emails.stream()
                .map(String::trim)
                .filter(email -> !email.isBlank())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    private ExpenseDto toExpenseDto(Expense expense, Map<String, BigDecimal> balances, String currentUserEmail) {
        String payerEmail = expense.getPaidBy().getEmail().toLowerCase(Locale.ROOT);
        BigDecimal share = expense.getAmount().divide(BigDecimal.valueOf(Math.max(1, expense.getGroup().getMembers().size())), 2, RoundingMode.HALF_UP);
        BigDecimal impact = payerEmail.equalsIgnoreCase(currentUserEmail)
            ? expense.getAmount().subtract(share)
            : share.negate();
        boolean positive = impact.compareTo(BigDecimal.ZERO) >= 0;

        return ExpenseDto.builder()
                .id(expense.getId())
            .groupId(expense.getGroup().getId())
                .paidByEmail(expense.getPaidBy().getEmail())
                .paidByName(expense.getPaidBy().getFirstname() + " " + expense.getPaidBy().getLastname())
                .description(expense.getDescription())
                .category(expense.getCategory())
                .desc(expense.getPaidBy().getFirstname() + " added " + expense.getDescription())
                .sub(expense.getCategory() + " • " + DATE_FORMAT.format(expense.getCreatedAt()))
                .amount(expense.getAmount().setScale(2, RoundingMode.HALF_UP))
            .share(impact.setScale(2, RoundingMode.HALF_UP))
                .positive(positive)
                .receiptUrl(expense.getReceiptUrl())
                .createdAt(expense.getCreatedAt())
                .build();
    }

    private BigDecimal sumExpenses(List<Expense> expenses) {
        return expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateBalanceForUser(Group group, String email) {
        Map<String, BigDecimal> balances = calculatePairwiseBalances(group, group.getExpenses(), email);
        Map<String, BigDecimal> settledByCounterpart = getSettledAmountsByCounterpart(group.getId(), email);
        applySettlementsToPairwiseBalances(balances, settledByCounterpart);

        // net balance for the user is the sum of pairwise values (what others owe them minus what they owe others)
        BigDecimal net = balances.entrySet().stream()
                .filter(e -> !e.getKey().equalsIgnoreCase(email))
                .map(Map.Entry::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        return net;
    }

    private Map<String, BigDecimal> getSettledAmountsByCounterpart(Long groupId, String currentEmail) {
        String curr = currentEmail == null ? "" : currentEmail.toLowerCase(Locale.ROOT);
        Map<String, BigDecimal> settled = new LinkedHashMap<>();

        for (BalanceSettlement settlement : balanceSettlementRepository.findByGroup_IdAndSettledAtIsNotNull(groupId)) {
            String emailA = settlement.getUserA().getEmail().toLowerCase(Locale.ROOT);
            String emailB = settlement.getUserB().getEmail().toLowerCase(Locale.ROOT);

            if (emailA.equals(curr)) {
                settled.put(emailB, settled.getOrDefault(emailB, BigDecimal.ZERO).add(settlement.getAmount()));
            } else if (emailB.equals(curr)) {
                settled.put(emailA, settled.getOrDefault(emailA, BigDecimal.ZERO).add(settlement.getAmount()));
            }
        }

        return settled;
    }

    private Map<String, BalanceSettlement> getPendingSettlementsByCounterpart(Long groupId, String currentEmail) {
        String curr = currentEmail == null ? "" : currentEmail.toLowerCase(Locale.ROOT);
        Map<String, BalanceSettlement> pending = new LinkedHashMap<>();

        for (BalanceSettlement settlement : balanceSettlementRepository.findByGroup_IdAndSettledAtIsNull(groupId)) {
            String emailA = settlement.getUserA().getEmail().toLowerCase(Locale.ROOT);
            String emailB = settlement.getUserB().getEmail().toLowerCase(Locale.ROOT);

            if (emailA.equals(curr)) {
                pending.put(emailB, settlement);
            } else if (emailB.equals(curr)) {
                pending.put(emailA, settlement);
            }
        }

        return pending;
    }

    private boolean isConfirmedByCurrentUser(BalanceSettlement settlement, String currentEmail) {
        if (settlement == null) {
            return false;
        }

        String curr = currentEmail == null ? "" : currentEmail.toLowerCase(Locale.ROOT);
        String emailA = settlement.getUserA().getEmail().toLowerCase(Locale.ROOT);
        if (emailA.equals(curr)) {
            return settlement.isConfirmedByUserA();
        }

        return settlement.isConfirmedByUserB();
    }

    private void applySettlementsToPairwiseBalances(Map<String, BigDecimal> balances, Map<String, BigDecimal> settledByCounterpart) {
        for (Map.Entry<String, BigDecimal> entry : settledByCounterpart.entrySet()) {
            String counterpart = entry.getKey();
            BigDecimal settledAmount = entry.getValue().setScale(2, RoundingMode.HALF_UP);
            BigDecimal current = balances.getOrDefault(counterpart, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

            if (current.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal adjusted = current.subtract(settledAmount);
                balances.put(counterpart, adjusted.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
            } else if (current.compareTo(BigDecimal.ZERO) < 0) {
                BigDecimal adjusted = current.add(settledAmount);
                balances.put(counterpart, adjusted.min(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
            }
        }
    }

    private Map<String, BigDecimal> calculatePairwiseBalances(Group group, List<Expense> expenses, String currentEmail) {
        Map<String, BigDecimal> balances = new LinkedHashMap<>();

        group.getMembers().forEach(member -> balances.put(member.getUser().getEmail().toLowerCase(Locale.ROOT), BigDecimal.ZERO));

        if (group.getMembers().isEmpty()) {
            return balances;
        }

        BigDecimal memberCount = BigDecimal.valueOf(group.getMembers().size());
        String curr = currentEmail == null ? "" : currentEmail.toLowerCase(Locale.ROOT);

        for (Expense expense : expenses) {
            BigDecimal share = expense.getAmount().divide(memberCount, 2, RoundingMode.HALF_UP);
            String payerEmail = expense.getPaidBy().getEmail().toLowerCase(Locale.ROOT);

            if (payerEmail.equals(curr)) {
                // current user paid: each other member owes current their share
                for (GroupMember member : group.getMembers()) {
                    String email = member.getUser().getEmail().toLowerCase(Locale.ROOT);
                    if (email.equals(curr)) continue;
                    balances.put(email, balances.getOrDefault(email, BigDecimal.ZERO).add(share));
                }
            } else if (balances.containsKey(payerEmail)) {
                // some other member paid: current user owes that payer their share
                if (!curr.isBlank() && !payerEmail.equals(curr)) {
                    balances.put(payerEmail, balances.getOrDefault(payerEmail, BigDecimal.ZERO).subtract(share));
                }
            }
        }

        return balances;
    }

    private Map<String, BigDecimal> calculateBalances(Group group, List<Expense> expenses) {
        Map<String, BigDecimal> balances = new LinkedHashMap<>();

        group.getMembers().forEach(member -> balances.put(member.getUser().getEmail().toLowerCase(Locale.ROOT), BigDecimal.ZERO));

        if (group.getMembers().isEmpty()) {
            return balances;
        }

        BigDecimal memberCount = BigDecimal.valueOf(group.getMembers().size());

        for (Expense expense : expenses) {
            BigDecimal share = expense.getAmount().divide(memberCount, 2, RoundingMode.HALF_UP);
            String payerEmail = expense.getPaidBy().getEmail().toLowerCase(Locale.ROOT);

            for (GroupMember member : group.getMembers()) {
                String email = member.getUser().getEmail().toLowerCase(Locale.ROOT);
                balances.put(email, balances.getOrDefault(email, BigDecimal.ZERO).subtract(share));
            }

            balances.put(payerEmail, balances.getOrDefault(payerEmail, BigDecimal.ZERO).add(expense.getAmount()));
        }

        return balances;
    }

    private String buildInitials(String firstname, String lastname) {
        String first = firstname == null || firstname.isBlank() ? "?" : firstname.substring(0, 1);
        String last = lastname == null || lastname.isBlank() ? "" : lastname.substring(0, 1);
        return (first + last).toUpperCase(Locale.ROOT);
    }
}