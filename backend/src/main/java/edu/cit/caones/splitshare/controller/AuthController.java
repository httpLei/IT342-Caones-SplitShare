package edu.cit.caones.splitshare.controller;

import edu.cit.caones.splitshare.dto.request.LoginRequest;
import edu.cit.caones.splitshare.dto.request.RegisterRequest;
import edu.cit.caones.splitshare.dto.response.ApiResponse;
import edu.cit.caones.splitshare.dto.response.AuthData;
import edu.cit.caones.splitshare.facade.AuthFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthFacade authFacade;

    /**
     * POST /api/v1/auth/register
     * Register a new user account.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthData>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthData data = authFacade.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(data));
    }

    /**
     * POST /api/v1/auth/login
     * Authenticate an existing user and return JWT tokens.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthData>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthData data = authFacade.login(request);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }
}
