package com.ms.springms.model.penjurian.presentasi;

import com.ms.springms.entity.penjurian.presentasi.PertanyaanPresentasi;
import com.ms.springms.entity.penjurian.presentasi.PointPresentasi;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PertanyaanWithPointRequest {
    private PertanyaanPresentasi pertanyaan;
    private List<PointPresentasi> points;

}
