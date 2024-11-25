package com.ms.springms.model.penjurian.yelyel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScoreSummary {
    private Long juriId;
    private Long deptId;
    private String deptName;
    private Double totalScore;
    private LocalDateTime createdAt;

}
