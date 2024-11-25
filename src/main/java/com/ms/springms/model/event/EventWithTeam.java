package com.ms.springms.model.event;

import com.ms.springms.model.team.TeamMemberDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventWithTeam {
    private Long eventId;
    private String eventName;
    private List<TeamMemberDTO> members;

}
