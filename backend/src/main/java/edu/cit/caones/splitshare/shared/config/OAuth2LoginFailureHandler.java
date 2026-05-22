package edu.cit.caones.splitshare.shared.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private final RedirectAwareOAuth2AuthorizationRequestResolver authorizationRequestResolver;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        String redirectUri = authorizationRequestResolver.extractRedirectUri(request.getParameter("state"));
        String target = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("error", "Google sign-in failed. Please try again.")
                .build()
                .encode()
                .toUriString();

        response.sendRedirect(target);
    }
}
