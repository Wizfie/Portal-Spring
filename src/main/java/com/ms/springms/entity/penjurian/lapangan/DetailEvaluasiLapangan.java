package com.ms.springms.entity.penjurian.lapangan;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "detail_evaluasi_lapangan")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetailEvaluasiLapangan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long teamId;
    private  Long eventId;
    private Long pertanyaanId;
    private String score;
    private Long userId;
    private LocalDateTime createdAt;


}
