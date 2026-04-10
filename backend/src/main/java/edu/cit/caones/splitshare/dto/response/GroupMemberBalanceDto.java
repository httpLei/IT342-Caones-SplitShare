package edu.cit.caones.splitshare.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class GroupMemberBalanceDto {
    private String name;
    private String initial;
    private BigDecimal amount;
    private boolean positive;
}