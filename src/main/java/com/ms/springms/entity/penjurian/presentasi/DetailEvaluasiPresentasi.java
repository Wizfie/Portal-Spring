package com.ms.springms.entity.penjurian.presentasi;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "detail_evaluasi_presentasi")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetailEvaluasiPresentasi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long pertanyaanId;
    private String score;
    private LocalDateTime createdAt;
    private Long teamId;
    private Long eventId;
    private Long userId;
}
