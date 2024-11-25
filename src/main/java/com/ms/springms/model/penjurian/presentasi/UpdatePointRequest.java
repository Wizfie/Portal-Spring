package com.ms.springms.model.penjurian.presentasi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePointRequest {
    private String pointPenilaian;
    private String scoreMaksimal;

}
