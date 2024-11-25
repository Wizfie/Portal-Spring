package com.ms.springms.entity.penjurian.lapangan.lapangan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointLapanganDTO {
    private Long id;
    private String jawaban;
    private Boolean active;

}
