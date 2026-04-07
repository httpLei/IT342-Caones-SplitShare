package edu.cit.caones.splitshare.strategy;

import org.springframework.stereotype.Component;

/**
 * Strategy for reactivating a user account.
 */
@Component
public class ReactivateUserStrategy implements UserStatusActionStrategy {

    @Override
    public boolean supports(boolean enabled) {
        return enabled;
    }

    @Override
    public String getAction() {
        return "USER_REACTIVATED";
    }

    @Override
    public String getDetails() {
        return "Admin reactivated user account";
    }
}
