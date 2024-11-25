package com.ms.springms.entity.penjurian.yelyel;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EvaluationYelyelDTO {

    private Double score;
    private Long deptId;
    private String deptName;
    private Long juriId;
    private String juriName;
    private LocalDateTime createdAt;

    public EvaluationYelyelDTO( Double score, Long deptId, String deptName, Long juriId, String juriName, LocalDateTime createdAt) {
        this.score = score;
        this.deptId = deptId;
        this.deptName = deptName;
        this.juriId = juriId;
        this.juriName = juriName;
        this.createdAt = createdAt;
    }


}
