package edu.cit.caones.splitshare.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UpdateGroupRequest {

    @Size(max = 100, message = "Group name must not exceed 100 characters")
    private String name;

    @Size(max = 20, message = "At most 20 members can be added")
    private List<@Email(message = "Each member email must be valid") String> memberEmails = new ArrayList<>();
}