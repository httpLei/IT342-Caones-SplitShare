package edu.cit.caones.splitshare.facade;

import edu.cit.caones.splitshare.dto.request.LoginRequest;
import edu.cit.caones.splitshare.dto.request.RegisterRequest;
import edu.cit.caones.splitshare.dto.response.AuthData;
import edu.cit.caones.splitshare.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Facade for authentication operations.
 * Provides a simplified, unified API for login and registration.
 * Decouples controllers from auth service complexity.
 */
@Component
@RequiredArgsConstructor
public class AuthFacade {

    private final AuthService authService;

    /**
     * Orchestrate user registration.
     * This is the single entry point for auth controllers to initiate registration.
     */
    public AuthData register(RegisterRequest request) {
        return authService.register(request);
    }

    /**
     * Orchestrate user login.
     * This is the single entry point for auth controllers to initiate login.
     */
    public AuthData login(LoginRequest request) {
        return authService.login(request);
    }
}
