package com.ms.springms.model.penjurian.yelyel;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EvaluasiResponse {

    private Long id;
    private Long pertanyaanId;
    private String pertanyaan;
    private Double score;
}