package com.ms.springms.model.team;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamDTO {
    private Long teamId;

    private String teamName;

    private String description;

    private Long userId;

    private LocalDate createdAt;
}
