package edu.cit.caones.splitshare.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SettleBalanceRequest {
    @NotBlank(message = "counterpartEmail is required")
    @Email(message = "counterpartEmail must be a valid email")
    private String counterpartEmail;
}
