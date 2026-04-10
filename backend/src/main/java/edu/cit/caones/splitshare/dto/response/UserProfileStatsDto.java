package edu.cit.caones.splitshare.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserProfileStatsDto {
    private int groupsCount;
    private int followersCount;
    private int followingCount;
}
