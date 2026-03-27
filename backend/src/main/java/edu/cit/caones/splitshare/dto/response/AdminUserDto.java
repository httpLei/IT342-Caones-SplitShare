package edu.cit.caones.splitshare.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminUserDto {
    private Long id;
    private String email;
    private String firstname;
    private String lastname;
    private String role;
    private boolean enabled;
    private String createdAt;
}
