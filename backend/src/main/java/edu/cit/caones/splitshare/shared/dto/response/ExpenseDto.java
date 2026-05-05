package edu.cit.caones.splitshare.shared.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class ExpenseDto {
    private Long id;
    private Long groupId;
    private String paidByEmail;
    private String paidByName;
    private String description;
    private String category;
    private String desc;
    private String sub;
    private BigDecimal amount;
    private BigDecimal share;
    private boolean positive;
    private String receiptUrl;
    private Instant createdAt;
}