package edu.cit.caones.splitshare.event;

import edu.cit.caones.splitshare.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a user's status (enabled/disabled) changes.
 * Observer pattern implementation for decoupling side effects.
 */
@Getter
public class UserStatusChangedEvent extends ApplicationEvent {

    private final String actorEmail;
    private final User user;
    private final String action;
    private final String details;

    public UserStatusChangedEvent(Object source, String actorEmail, User user, String action, String details) {
        super(source);
        this.actorEmail = actorEmail;
        this.user = user;
        this.action = action;
        this.details = details;
    }
}
