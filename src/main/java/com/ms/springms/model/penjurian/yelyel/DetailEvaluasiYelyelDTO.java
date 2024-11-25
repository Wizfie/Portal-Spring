package com.ms.springms.model.penjurian.yelyel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetailEvaluasiYelyelDTO {

    private Long id;
    private Long pertanyaanId;
    private String score;
    private Long deptId;
    private String deptName;
    private Long juriId;
    private String juriName;
    private LocalDateTime createdAt;
    private String pertanyaan;

}
