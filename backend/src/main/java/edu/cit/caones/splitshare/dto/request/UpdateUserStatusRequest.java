package edu.cit.caones.splitshare.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserStatusRequest {

    @NotNull(message = "enabled is required")
    private Boolean enabled;
}
