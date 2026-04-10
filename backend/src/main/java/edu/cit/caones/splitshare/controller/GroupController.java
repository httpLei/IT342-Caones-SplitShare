package edu.cit.caones.splitshare.controller;

import edu.cit.caones.splitshare.dto.request.CreateExpenseRequest;
import edu.cit.caones.splitshare.dto.request.CreateGroupRequest;
import edu.cit.caones.splitshare.dto.request.UpdateExpenseRequest;
import edu.cit.caones.splitshare.dto.request.UpdateGroupRequest;
import edu.cit.caones.splitshare.dto.response.ApiResponse;
import edu.cit.caones.splitshare.dto.response.GroupDetailsDto;
import edu.cit.caones.splitshare.dto.response.GroupSummaryDto;
import edu.cit.caones.splitshare.service.ExpenseService;
import edu.cit.caones.splitshare.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final ExpenseService expenseService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<GroupSummaryDto>>> getMyGroups(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(groupService.getMyGroups(authentication.getName())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GroupSummaryDto>> createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            Authentication authentication) {

        GroupSummaryDto created = groupService.createGroup(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupDetailsDto>> getGroup(
            @PathVariable Long groupId,
            Authentication authentication) {

        return ResponseEntity.ok(ApiResponse.ok(groupService.getGroupDetails(groupId, authentication.getName())));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupDetailsDto>> updateGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody UpdateGroupRequest request,
            Authentication authentication) {

        GroupDetailsDto updated = groupService.updateGroup(groupId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }

    @PostMapping(value = "/{groupId}/expenses", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<GroupDetailsDto>> addExpense(
            @PathVariable Long groupId,
            @Valid @RequestPart("data") CreateExpenseRequest request,
            @RequestPart(value = "receipt", required = false) MultipartFile receipt,
            Authentication authentication) {

        GroupDetailsDto updated = expenseService.addExpense(groupId, request, receipt, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(updated));
    }

    @PutMapping(value = "/{groupId}/expenses/{expenseId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<GroupDetailsDto>> updateExpense(
            @PathVariable Long groupId,
            @PathVariable Long expenseId,
            @Valid @RequestPart("data") UpdateExpenseRequest request,
            @RequestPart(value = "receipt", required = false) MultipartFile receipt,
            Authentication authentication) {

        GroupDetailsDto updated = expenseService.updateExpense(groupId, expenseId, request, receipt, authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }

    @DeleteMapping("/{groupId}/expenses/{expenseId}")
    public ResponseEntity<ApiResponse<GroupDetailsDto>> deleteExpense(
            @PathVariable Long groupId,
            @PathVariable Long expenseId,
            Authentication authentication) {

        GroupDetailsDto updated = expenseService.deleteExpense(groupId, expenseId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }
}