package edu.cit.caones.splitshare.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class ExpenseDto {
    private Long id;
    private String desc;
    private String sub;
    private BigDecimal amount;
    private BigDecimal share;
    private boolean positive;
    private Instant createdAt;
}