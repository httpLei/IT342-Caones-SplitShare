package edu.cit.caones.splitshare.service;

import edu.cit.caones.splitshare.dto.response.UserActivityDto;
import edu.cit.caones.splitshare.entity.Expense;
import edu.cit.caones.splitshare.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserActivityService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);

    private final ExpenseRepository expenseRepository;

    @Transactional(readOnly = true)
    public List<UserActivityDto> getMyHistory(String currentUserEmail) {
        return expenseRepository.findUserActivity(currentUserEmail)
                .stream()
                .map(expense -> toDto(expense, currentUserEmail))
                .toList();
    }

    private UserActivityDto toDto(Expense expense, String currentUserEmail) {
        int memberCount = Math.max(1, expense.getGroup().getMembers().size());
        BigDecimal share = expense.getAmount().divide(BigDecimal.valueOf(memberCount), 2, RoundingMode.HALF_UP);

        boolean isPayer = expense.getPaidBy().getEmail().equalsIgnoreCase(currentUserEmail);
        BigDecimal impact = isPayer ? expense.getAmount().subtract(share) : share.negate();

        String desc = isPayer
                ? "You added " + expense.getDescription()
                : expense.getPaidBy().getFirstname() + " added " + expense.getDescription();

        String sub = expense.getGroup().getName() + " - " + DATE_FORMAT.format(expense.getCreatedAt());

        return UserActivityDto.builder()
                .id(expense.getId())
                .desc(desc)
                .sub(sub)
                .amount(expense.getAmount().setScale(2, RoundingMode.HALF_UP))
                .share(impact.setScale(2, RoundingMode.HALF_UP))
                .positive(impact.compareTo(BigDecimal.ZERO) >= 0)
                .createdAt(expense.getCreatedAt())
                .build();
    }
}