package com.ms.springms.model.penjurian.presentasi;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointPresentasiDTO {
    private Long id;
    private String pointPenilaian;
    private String scoreMaksimal;
    private boolean active;
}
