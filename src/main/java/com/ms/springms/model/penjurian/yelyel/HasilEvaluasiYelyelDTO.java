package com.ms.springms.model.penjurian.yelyel;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HasilEvaluasiYelyelDTO {

    private Long id;
    private Long deptId;
    private String deptName;
    private Long juriId;
    private String juriName;
    private Long pertanyaanId;
    @Column(columnDefinition = "TEXT")
    private String pertanyaan;
    private String score;
    private LocalDateTime createdAt;

}
