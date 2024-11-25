package com.ms.springms.entity.penjurian.lapangan.lapangan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class LapanganDataDTO {
    private Long faseId;
    private  String faseName;
    private String faseType;
    private List<KriteriaDTO> kriteriaList;

}
