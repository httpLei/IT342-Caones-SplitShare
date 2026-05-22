package edu.cit.caones.splitshare.shared.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private final RedirectAwareOAuth2AuthorizationRequestResolver authorizationRequestResolver;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        // Log the exception details for easier diagnosis (includes cause and message)
        log.error("OAuth2 login failure (state={})", request.getParameter("state"), exception);

        String redirectUri = authorizationRequestResolver.extractRedirectUri(request.getParameter("state"));
        String target = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("error", "Google sign-in failed. Please try again.")
                .build()
                .encode()
                .toUriString();

        response.sendRedirect(target);
    }
}
