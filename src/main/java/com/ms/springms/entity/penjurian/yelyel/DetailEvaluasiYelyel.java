package com.ms.springms.entity.penjurian.yelyel;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "detail_evaluasi_yelyel")

public class DetailEvaluasiYelyel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long pertanyaanId;
    private String score;
    private Long deptId;
    private String deptName;
    private Long juriId;
    private LocalDateTime createdAt;




}
