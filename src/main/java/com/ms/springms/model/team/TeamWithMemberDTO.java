package com.ms.springms.model.team;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamWithMemberDTO {

    private Long teamId;

    private String teamName;

    private Long userId;

    private String description;

    private LocalDate createdAt;

    List<TeamMemberDTO> members;
}
