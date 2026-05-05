package edu.cit.caones.splitshare.features.auth.facade;

import edu.cit.caones.splitshare.shared.dto.request.LoginRequest;
import edu.cit.caones.splitshare.shared.dto.request.RegisterRequest;
import edu.cit.caones.splitshare.shared.dto.response.AuthData;
import edu.cit.caones.splitshare.features.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthFacade {

    private final AuthService authService;

    public AuthData register(RegisterRequest request) {
        return authService.register(request);
    }

    public AuthData login(LoginRequest request) {
        return authService.login(request);
    }
}
