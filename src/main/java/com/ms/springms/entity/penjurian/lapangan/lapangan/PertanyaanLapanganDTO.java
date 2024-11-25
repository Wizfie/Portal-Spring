package com.ms.springms.entity.penjurian.lapangan.lapangan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PertanyaanLapanganDTO {
    private Long id;
    private String pertanyaan;
    private Boolean active;
    private List<PointLapanganDTO> points;
}
