package com.ms.springms.model.team;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TeamMemberDTO {
    private Long teamMemberId;
    private String memberName;
    private String memberPosition;
    private String nip;


    public TeamMemberDTO(Long teamMemberId ,String memberName, String memberPosition , String nip) {
        this.teamMemberId = teamMemberId;
        this.memberName = memberName;
        this.memberPosition = memberPosition;
        this.nip = nip;
        }

}
