package com.ms.springms.model.penjurian;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TotalEvaluasiDTO {

    private  Long id;

    private Long teamId;
    private Long eventId;
    private Long userId;
    private String dept;

    private Long totalLapangan;
    private Long totalPresentasi;


    private String teamName;
    private String eventName;
    private String username;

    private String scoreLapangan;
    private String scorePresentasi;

    private LocalDateTime createdAtLapangan;
    private LocalDateTime createdAtPresentasi;
}
