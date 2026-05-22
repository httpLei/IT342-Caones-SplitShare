package edu.cit.caones.splitshare.shared.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class RedirectAwareOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private static final String AUTHORIZATION_BASE_URI = "/oauth2/authorization";
    private static final String REDIRECT_PARAM = "redirect_uri";
    private static final String STATE_SEPARATOR = ".";

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuth2RedirectProperties redirectProperties;

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        return customize(defaultResolver().resolve(request), request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        return customize(defaultResolver().resolve(request, clientRegistrationId), request);
    }

    public String extractRedirectUri(String state) {
        if (state == null || !state.contains(STATE_SEPARATOR)) {
            return redirectProperties.getDefaultRedirectUri();
        }

        String encodedRedirect = state.substring(0, state.indexOf(STATE_SEPARATOR));
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(encodedRedirect);
            String redirectUri = new String(decoded, StandardCharsets.UTF_8);
            return isAllowedRedirect(redirectUri) ? redirectUri : redirectProperties.getDefaultRedirectUri();
        } catch (IllegalArgumentException ex) {
            return redirectProperties.getDefaultRedirectUri();
        }
    }

    private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest request, HttpServletRequest servletRequest) {
        if (request == null) {
            return null;
        }

        String redirectUri = servletRequest.getParameter(REDIRECT_PARAM);
        if (!isAllowedRedirect(redirectUri)) {
            redirectUri = redirectProperties.getDefaultRedirectUri();
        }

        String encodedRedirect = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(redirectUri.getBytes(StandardCharsets.UTF_8));
        String state = encodedRedirect + STATE_SEPARATOR + request.getState();

        return OAuth2AuthorizationRequest.from(request)
                .state(state)
                .build();
    }

    private OAuth2AuthorizationRequestResolver defaultResolver() {
        return new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, AUTHORIZATION_BASE_URI);
    }

    private boolean isAllowedRedirect(String redirectUri) {
        if (redirectUri == null || redirectUri.isBlank()) {
            return false;
        }

        try {
            var uri = UriComponentsBuilder.fromUriString(redirectUri).build().toUri();
            String scheme = uri.getScheme();
            String host = uri.getHost();
            return ("splitshare".equalsIgnoreCase(scheme) && "oauth2".equalsIgnoreCase(host))
                    || (("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                    && ("localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host)));
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
