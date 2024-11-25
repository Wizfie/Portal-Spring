package com.ms.springms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "report_log")
public class ReportLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer year;
    private String noUrut;
    private String bulan;
    private String generatedBy; // Misalnya, untuk mencatat siapa yang menghasilkan laporan
    private LocalDateTime generatedDate; // Tanggal dan waktu laporan dihasilkan

}
