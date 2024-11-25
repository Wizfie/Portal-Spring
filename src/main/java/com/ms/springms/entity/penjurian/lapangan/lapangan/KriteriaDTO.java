package com.ms.springms.entity.penjurian.lapangan.lapangan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KriteriaDTO {
    private Long id;
    private String name;
    private Long faseId;
    private List<SubKriteriaDTO> subKriteriaList;
}
