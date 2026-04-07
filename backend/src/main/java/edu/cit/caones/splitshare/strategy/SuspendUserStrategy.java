package edu.cit.caones.splitshare.strategy;

import org.springframework.stereotype.Component;

/**
 * Strategy for suspending a user account.
 */
@Component
public class SuspendUserStrategy implements UserStatusActionStrategy {

    @Override
    public boolean supports(boolean enabled) {
        return !enabled;
    }

    @Override
    public String getAction() {
        return "USER_SUSPENDED";
    }

    @Override
    public String getDetails() {
        return "Admin suspended user account";
    }
}
