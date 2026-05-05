package edu.cit.caones.splitshare.features.auth.controller;

import edu.cit.caones.splitshare.shared.dto.request.LoginRequest;
import edu.cit.caones.splitshare.shared.dto.request.RegisterRequest;
import edu.cit.caones.splitshare.shared.dto.response.ApiResponse;
import edu.cit.caones.splitshare.shared.dto.response.AuthData;
import edu.cit.caones.splitshare.features.auth.facade.AuthFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthFacade authFacade;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthData>> register(@Valid @RequestBody RegisterRequest request) {
        AuthData data = authFacade.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(data));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthData>> login(@Valid @RequestBody LoginRequest request) {
        AuthData data = authFacade.login(request);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }
}
