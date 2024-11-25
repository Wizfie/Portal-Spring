package com.ms.springms.model.penjurian;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HasilEvaluasiDTO {

    private Long id;
    private String score;
    private Long userId;
    private Long pertanyaanId;
    private Long teamId;
    private Long eventId;
    private LocalDateTime createdAt;
    private String username;
    private String teamName;
    private String eventName;
    @Column(columnDefinition = "TEXT")
    private String pertanyaan;

}
