package edu.cit.caones.splitshare.strategy;

/**
 * Strategy pattern for user status transitions.
 * Encapsulates behavior and metadata for different status actions.
 */
public interface UserStatusActionStrategy {

    /**
     * Check if this strategy handles the given enabled state.
     */
    boolean supports(boolean enabled);

    /**
     * Get the action code for audit logging.
     */
    String getAction();

    /**
     * Get the action description for audit logging.
     */
    String getDetails();
}
