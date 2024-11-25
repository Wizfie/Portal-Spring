package com.ms.springms.model.penjurian.yelyel;

import java.util.ArrayList;
import java.util.List;

public class DeptScoreDTO {
    private Long deptId;
    private String deptName;
    private Double totalScore; // Hasil penjumlahan dari skor setiap juri
    private List<JuriScoreDTO> juris; // Daftar juri
    private int year; // Tahun createdAt

    public DeptScoreDTO(Long deptId, String deptName, int year) {
        this.deptId = deptId;
        this.deptName = deptName;
        this.year = year;
        this.totalScore = 0.0; // Akan dihitung berdasarkan skor juri
        this.juris = new ArrayList<>(); // Daftar juri akan ditambahkan kemudian
    }

    // Tambahkan getter dan setter
    public void addJuri(JuriScoreDTO juri) {
        this.juris.add(juri);
        this.totalScore += juri.getScore(); // Menambahkan skor juri ke totalScore
    }

    public Long getDeptId() { return deptId; }
    public String getDeptName() { return deptName; }
    public Double getTotalScore() { return totalScore; }
    public List<JuriScoreDTO> getJuris() { return juris; }
    public int getYear() { return year; }
}
