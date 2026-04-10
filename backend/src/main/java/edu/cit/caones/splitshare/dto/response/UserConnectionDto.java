package edu.cit.caones.splitshare.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserConnectionDto {
    private Long id;
    private String email;
    private String firstname;
    private String lastname;
    private boolean following;
    private boolean followedBy;
    private boolean mutual;
}