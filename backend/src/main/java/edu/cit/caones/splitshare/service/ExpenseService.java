package edu.cit.caones.splitshare.service;

import edu.cit.caones.splitshare.dto.request.CreateExpenseRequest;
import edu.cit.caones.splitshare.dto.request.UpdateExpenseRequest;
import edu.cit.caones.splitshare.dto.response.ExpenseDto;
import edu.cit.caones.splitshare.dto.response.GroupDetailsDto;
import edu.cit.caones.splitshare.entity.Expense;
import edu.cit.caones.splitshare.entity.Group;
import edu.cit.caones.splitshare.entity.User;
import edu.cit.caones.splitshare.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);

    private final ExpenseRepository expenseRepository;
    private final GroupService groupService;
    private final ReceiptStorageService receiptStorageService;

    @Transactional
    public GroupDetailsDto addExpense(Long groupId, CreateExpenseRequest request, MultipartFile receipt, String currentUserEmail) {
        Group group = groupService.getAccessibleGroup(groupId, currentUserEmail);
        User payer = groupService.getUserByEmail(currentUserEmail);

        String receiptUrl = storeReceipt(receipt);

        Expense expense = Expense.builder()
                .group(group)
                .paidBy(payer)
                .description(request.getDescription().trim())
                .category(request.getCategory().trim())
                .amount(request.getAmount().setScale(2, RoundingMode.HALF_UP))
                .receiptUrl(receiptUrl)
                .build();

        expenseRepository.save(expense);
        return groupService.getGroupDetails(groupId, currentUserEmail);
    }

    @Transactional
    public GroupDetailsDto updateExpense(Long groupId, Long expenseId, UpdateExpenseRequest request, MultipartFile receipt, String currentUserEmail) {
        Group group = groupService.getAccessibleGroup(groupId, currentUserEmail);
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Expense not found"));

        if (!expense.getGroup().getId().equals(group.getId())) {
            throw new IllegalArgumentException("Expense does not belong to this group");
        }

        expense.setDescription(request.getDescription().trim());
        expense.setCategory(request.getCategory().trim());
        expense.setAmount(request.getAmount().setScale(2, RoundingMode.HALF_UP));

        String receiptUrl = storeReceipt(receipt);
        if (receiptUrl != null) {
            expense.setReceiptUrl(receiptUrl);
        }

        expenseRepository.save(expense);
        return groupService.getGroupDetails(groupId, currentUserEmail);
    }

    @Transactional
    public GroupDetailsDto deleteExpense(Long groupId, Long expenseId, String currentUserEmail) {
        Group group = groupService.getAccessibleGroup(groupId, currentUserEmail);
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Expense not found"));

        if (!expense.getGroup().getId().equals(group.getId())) {
            throw new IllegalArgumentException("Expense does not belong to this group");
        }

        expenseRepository.delete(expense);
        return groupService.getGroupDetails(groupId, currentUserEmail);
    }

    @Transactional(readOnly = true)
    public ExpenseDto getExpenseDetails(Long expenseId, String currentUserEmail) {
        Expense expense = getAccessibleExpense(expenseId, currentUserEmail);
        return toExpenseDto(expense, currentUserEmail);
    }

    @Transactional
    public ExpenseDto updateExpense(Long expenseId, UpdateExpenseRequest request, MultipartFile receipt, String currentUserEmail) {
        Expense expense = getAccessibleExpense(expenseId, currentUserEmail);

        expense.setDescription(request.getDescription().trim());
        expense.setCategory(request.getCategory().trim());
        expense.setAmount(request.getAmount().setScale(2, RoundingMode.HALF_UP));

        String receiptUrl = storeReceipt(receipt);
        if (receiptUrl != null) {
            expense.setReceiptUrl(receiptUrl);
        }

        Expense saved = expenseRepository.save(expense);
        return toExpenseDto(saved, currentUserEmail);
    }

    @Transactional
    public void deleteExpense(Long expenseId, String currentUserEmail) {
        Expense expense = getAccessibleExpense(expenseId, currentUserEmail);
        expenseRepository.delete(expense);
    }

    private Expense getAccessibleExpense(Long expenseId, String currentUserEmail) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Expense not found"));

        groupService.getAccessibleGroup(expense.getGroup().getId(), currentUserEmail);
        return expense;
    }

    private ExpenseDto toExpenseDto(Expense expense, String currentUserEmail) {
        int memberCount = Math.max(1, expense.getGroup().getMembers().size());
        BigDecimal share = expense.getAmount().divide(BigDecimal.valueOf(memberCount), 2, RoundingMode.HALF_UP);

        boolean isPayer = expense.getPaidBy().getEmail().equalsIgnoreCase(currentUserEmail);
        BigDecimal impact = isPayer ? expense.getAmount().subtract(share) : share.negate();

        String desc = isPayer
                ? "You added " + expense.getDescription()
                : expense.getPaidBy().getFirstname() + " added " + expense.getDescription();

        String sub = expense.getGroup().getName() + " - " + DATE_FORMAT.format(expense.getCreatedAt());

        return ExpenseDto.builder()
                .id(expense.getId())
                .groupId(expense.getGroup().getId())
                .description(expense.getDescription())
                .category(expense.getCategory())
                .desc(desc)
                .sub(sub)
                .amount(expense.getAmount().setScale(2, RoundingMode.HALF_UP))
                .share(impact)
                .positive(impact.compareTo(BigDecimal.ZERO) >= 0)
                .receiptUrl(expense.getReceiptUrl())
                .createdAt(expense.getCreatedAt())
                .build();
    }

    private String storeReceipt(MultipartFile receipt) {
        if (receipt == null || receipt.isEmpty()) {
            return null;
        }

        try {
            return receiptStorageService.store(receipt);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to store receipt: " + ex.getMessage());
        }
    }
}