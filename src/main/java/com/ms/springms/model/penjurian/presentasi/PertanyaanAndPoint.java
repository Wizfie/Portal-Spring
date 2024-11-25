package com.ms.springms.model.penjurian.presentasi;

import com.ms.springms.entity.penjurian.presentasi.PointPresentasi;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PertanyaanAndPoint {

    private Long id;
    private String pertanyaan;
    private String type;
    private boolean active;
    private List<PointPresentasiDTO> point;
}
