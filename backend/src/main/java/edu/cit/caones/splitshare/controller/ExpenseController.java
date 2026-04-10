package edu.cit.caones.splitshare.controller;

import edu.cit.caones.splitshare.dto.request.UpdateExpenseRequest;
import edu.cit.caones.splitshare.dto.response.ApiResponse;
import edu.cit.caones.splitshare.dto.response.ExpenseDto;
import edu.cit.caones.splitshare.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<ExpenseDto>> getExpense(
            @PathVariable Long expenseId,
            Authentication authentication) {

        ExpenseDto expense = expenseService.getExpenseDetails(expenseId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(expense));
    }

    @PutMapping(value = "/{expenseId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ExpenseDto>> updateExpense(
            @PathVariable Long expenseId,
            @Valid @RequestPart("data") UpdateExpenseRequest request,
            @RequestPart(value = "receipt", required = false) MultipartFile receipt,
            Authentication authentication) {

        ExpenseDto updated = expenseService.updateExpense(expenseId, request, receipt, authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<String>> deleteExpense(
            @PathVariable Long expenseId,
            Authentication authentication) {

        expenseService.deleteExpense(expenseId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok("Expense deleted"));
    }
}