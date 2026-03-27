package edu.cit.caones.splitshare.config;

import edu.cit.caones.splitshare.entity.Role;
import edu.cit.caones.splitshare.entity.User;
import edu.cit.caones.splitshare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${application.admin.bootstrap.enabled:false}")
    private boolean enabled;

    @Value("${application.admin.bootstrap.email:}")
    private String email;

    @Value("${application.admin.bootstrap.password:}")
    private String password;

    @Value("${application.admin.bootstrap.firstname:Super}")
    private String firstname;

    @Value("${application.admin.bootstrap.lastname:Admin}")
    private String lastname;

    @Override
    public void run(String... args) {
        if (!enabled) {
            return;
        }

        String normalizedEmail = normalize(email);
        if (normalizedEmail.isBlank() || password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "Admin bootstrap is enabled but ADMIN_BOOTSTRAP_EMAIL or ADMIN_BOOTSTRAP_PASSWORD is missing."
            );
        }

        userRepository.findByEmail(normalizedEmail).ifPresentOrElse(existingUser -> {
            existingUser.setRole(Role.ROLE_ADMIN);
            existingUser.setEnabled(true);
            existingUser.setPassword(passwordEncoder.encode(password));
            existingUser.setFirstname(blankSafe(firstname, "Super"));
            existingUser.setLastname(blankSafe(lastname, "Admin"));
            userRepository.save(existingUser);
            log.info("Synchronized bootstrap admin account: {}", normalizedEmail);
        }, () -> {
            User adminUser = User.builder()
                    .firstname(blankSafe(firstname, "Super"))
                    .lastname(blankSafe(lastname, "Admin"))
                    .email(normalizedEmail)
                    .password(passwordEncoder.encode(password))
                    .role(Role.ROLE_ADMIN)
                    .build();

            userRepository.save(adminUser);
            log.info("Created bootstrap admin user: {}", normalizedEmail);
        });
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String blankSafe(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }
}
