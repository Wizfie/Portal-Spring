package com.ms.springms.model.penjurian.lapangan;

import lombok.Data;

@Data
public class DetailEvaluasiLapanganDTO {

    private Long id;
    private Long pertanyaanId;
    private String pertanyaan;
    private Double score;
}
