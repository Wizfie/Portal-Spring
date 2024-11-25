package com.ms.springms.model.penjurian.presentasi;

import lombok.Data;

@Data
    public class DetailEvaluasiPresentasiDTO {

    private Long id;
    private Long pertanyaanId;
    private String pertanyaan;
    private Double score;
}
