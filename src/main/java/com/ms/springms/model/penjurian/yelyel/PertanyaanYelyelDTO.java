package com.ms.springms.model.penjurian.yelyel;

import com.ms.springms.entity.penjurian.yelyel.PointPenilaianYelyel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PertanyaanYelyelDTO {

    private Long id;
    private String name;
    private boolean active;
    List<PointYelyelDTO> points;
}

