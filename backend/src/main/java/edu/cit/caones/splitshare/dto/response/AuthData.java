package edu.cit.caones.splitshare.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthData {
    private UserDto user;
    private String accessToken;
    private String refreshToken;
}
