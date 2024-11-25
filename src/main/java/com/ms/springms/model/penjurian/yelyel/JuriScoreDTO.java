package com.ms.springms.model.penjurian.yelyel;

public class JuriScoreDTO {
    private Long juriId;
    private String juriName;
    private Double score;

    public JuriScoreDTO(Long juriId, String juriName, Double score) {
        this.juriId = juriId;
        this.juriName = juriName;
        this.score = score;
    }

    // Tambahkan getter dan setter
    public Long getJuriId() { return juriId; }
    public String getJuriName() { return juriName; }
    public Double getScore() { return score; }
}
