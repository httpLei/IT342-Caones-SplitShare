package edu.cit.caones.splitshare.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
    private String email;
    private String firstname;
    private String lastname;
    private String role;
}
