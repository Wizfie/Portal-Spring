package com.ms.springms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "total_score_penilaian")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TotalPenilaian {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long teamId;
    private Long eventId;
    private String scoreLapangan;
    private String scorePresentasi;
    private Long userId;
    private LocalDateTime createdAtLapangan;
    private LocalDateTime createdAtPresentasi;


}
