package com.ms.springms.model.registration;

import com.ms.springms.model.team.TeamMemberDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationTeamsDTO {
    private Long teamId;
    private String teamName;
    private String userId;
    private String username;
    private String judul;
    private List<TeamMemberDTO> teamMember;

    }
