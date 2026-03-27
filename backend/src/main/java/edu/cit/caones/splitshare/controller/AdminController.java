package edu.cit.caones.splitshare.controller;

import edu.cit.caones.splitshare.dto.request.UpdateUserStatusRequest;
import edu.cit.caones.splitshare.dto.response.AdminAuditLogDto;
import edu.cit.caones.splitshare.dto.response.AdminUserDto;
import edu.cit.caones.splitshare.dto.response.ApiResponse;
import edu.cit.caones.splitshare.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<AdminUserDto>>> getUsers() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.listUsers()));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<AdminUserDto>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request,
            Authentication authentication) {

        AdminUserDto updated = adminService.updateUserStatus(id, request.getEnabled(), authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<AdminAuditLogDto>>> getAuditLogs(
            @RequestParam(defaultValue = "50") int limit) {

        return ResponseEntity.ok(ApiResponse.ok(adminService.listAuditLogs(limit)));
    }
}
