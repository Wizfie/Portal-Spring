package com.ms.springms.model.team;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamMemberResponse {
    private Long teamMemberId;
    private String memberName;
    private String memberPosition;
    private Long eventId;
    private Long teamId;
    private String nip;
}
