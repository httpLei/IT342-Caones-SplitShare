package edu.cit.caones.splitshare.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OAuth2RedirectProperties {

    @Value("${application.oauth2.default-redirect-uri}")
    private String defaultRedirectUri;

    public String getDefaultRedirectUri() {
        return defaultRedirectUri;
    }
}
