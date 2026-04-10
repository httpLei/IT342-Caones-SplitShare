package edu.cit.caones.splitshare.service;

import edu.cit.caones.splitshare.dto.request.CreateExpenseRequest;
import edu.cit.caones.splitshare.dto.response.GroupDetailsDto;
import edu.cit.caones.splitshare.entity.Expense;
import edu.cit.caones.splitshare.entity.Group;
import edu.cit.caones.splitshare.entity.User;
import edu.cit.caones.splitshare.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final GroupService groupService;

    @Transactional
    public GroupDetailsDto addExpense(Long groupId, CreateExpenseRequest request, String currentUserEmail) {
        Group group = groupService.getAccessibleGroup(groupId, currentUserEmail);
        User payer = groupService.getUserByEmail(currentUserEmail);

        Expense expense = Expense.builder()
                .group(group)
                .paidBy(payer)
                .description(request.getDescription().trim())
                .category(request.getCategory().trim())
                .amount(request.getAmount().setScale(2, RoundingMode.HALF_UP))
                .build();

        expenseRepository.save(expense);
        return groupService.getGroupDetails(groupId, currentUserEmail);
    }
}