package com.ms.springms.model.penjurian.yelyel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointYelyelDTO {
    Long id;
    private String pertanyaan;
    private double scoreMaksimal;
    private boolean active;
}
