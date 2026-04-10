package edu.cit.caones.splitshare.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class GroupSummaryDto {
    private Long id;
    private String name;
    private List<String> members;
    private BigDecimal total;
    private BigDecimal balance;
    private Instant createdAt;
}