package com.ms.springms.entity.penjurian.lapangan.lapangan;

import lombok.Data;

import java.util.List;

@Data
public class SubKriteriaDTO {
    private Long id;
    private String name;
    private  Long kriteriaId;
    private List<PertanyaanDTO> pertanyaanList;
    private Long faseId;
    private String faseName;  // Nama Fase
    private String faseType;  // Tipe Fase

}
