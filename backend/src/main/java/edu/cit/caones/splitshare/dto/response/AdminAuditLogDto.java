package edu.cit.caones.splitshare.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminAuditLogDto {
    private Long id;
    private String action;
    private String actorEmail;
    private Long targetUserId;
    private String targetUserEmail;
    private String details;
    private String createdAt;
}
