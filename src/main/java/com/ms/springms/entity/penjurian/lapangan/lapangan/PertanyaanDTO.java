package com.ms.springms.entity.penjurian.lapangan.lapangan;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PertanyaanDTO {
    private Long id;
    private String pertanyaan;
    private Long subKriteriaId;
    private List<String> jawabanList;
}
