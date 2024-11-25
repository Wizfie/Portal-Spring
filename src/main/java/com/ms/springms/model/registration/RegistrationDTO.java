package com.ms.springms.model.registration;

import com.ms.springms.entity.Event;
import com.ms.springms.entity.Team;
import com.ms.springms.model.event.EventDTO;
import com.ms.springms.model.team.TeamDTO;
import com.ms.springms.model.team.TeamMemberDTO;
import com.ms.springms.model.team.TeamWithMemberDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationDTO {
    private Long registrationId;
    private String judul;
    private Long createdBy;
    private String username;
    private String registrationStatus;
    private int rank;
    private List<String> fileNames;
    private Double finalScore;

    private LocalDateTime createdAt;
    private TeamWithMemberDTO team;
    private EventDTO event;


}
