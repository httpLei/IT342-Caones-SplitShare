package edu.cit.caones.splitshare.shared.config;

import edu.cit.caones.splitshare.features.auth.service.GoogleAuthService;
import edu.cit.caones.splitshare.shared.dto.response.AuthData;
import edu.cit.caones.splitshare.shared.dto.response.UserDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final GoogleAuthService googleAuthService;
    private final RedirectAwareOAuth2AuthorizationRequestResolver authorizationRequestResolver;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String email = oauthUser.getAttribute("email");
        String firstName = firstNonBlank(oauthUser.getAttribute("given_name"), oauthUser.getAttribute("name"), "Google");
        String lastName = firstNonBlank(oauthUser.getAttribute("family_name"), "User");

        AuthData authData = googleAuthService.loginWithGoogle(email, firstName, lastName);
        UserDto user = authData.getUser();
        String redirectUri = authorizationRequestResolver.extractRedirectUri(request.getParameter("state"));

        String target = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("accessToken", authData.getAccessToken())
                .queryParam("refreshToken", authData.getRefreshToken())
                .queryParam("email", user.getEmail())
                .queryParam("firstname", user.getFirstname())
                .queryParam("lastname", user.getLastname())
                .queryParam("role", user.getRole())
                .queryParamIfPresent("currency", java.util.Optional.ofNullable(user.getCurrency()))
                .build()
                .encode()
                .toUriString();

        response.sendRedirect(target);
    }

    private String firstNonBlank(Object... values) {
        for (Object value : values) {
            if (value instanceof String text && !text.isBlank()) {
                return text;
            }
        }
        return "Google";
    }
}
