package com.ms.springms.entity.penjurian.lapangan.lapangan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FaseLapanganDTO {
    private Long id;
    private String name;
    private String type;
    private boolean active;
    private List<KriteriaLapanganDTO> kriteriaLapangan;
}
