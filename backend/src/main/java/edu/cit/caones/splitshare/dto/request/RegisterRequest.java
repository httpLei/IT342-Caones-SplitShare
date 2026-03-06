package edu.cit.caones.splitshare.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    private String firstname;

    @NotBlank(message = "Last name is required")
    private String lastname;

    @Email(message = "Email must be a valid email address")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Must be at least 8 characters")
    private String password;
}
