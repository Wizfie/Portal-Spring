package com.ms.springms.model.penjurian.totalScore;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserScoreDTO {
    private Long id;
    private Long teamId;
    private String teamName;
    private Long eventId;
    private String eventName;
    private Long userId;
    private String username;
    private String scoreLapangan;
    private String scorePresentasi;
//    private String scoreYelyel;
}
