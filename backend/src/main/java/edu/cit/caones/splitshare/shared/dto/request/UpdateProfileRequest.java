package edu.cit.caones.splitshare.shared.dto.request;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String firstname;
    private String lastname;
    private String currency;
}
