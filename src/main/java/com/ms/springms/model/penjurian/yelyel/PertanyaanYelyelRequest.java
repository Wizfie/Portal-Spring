package com.ms.springms.model.penjurian.yelyel;

import com.ms.springms.entity.penjurian.yelyel.KriteriaYelyel;
import com.ms.springms.entity.penjurian.yelyel.PointPenilaianYelyel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.internal.build.AllowNonPortable;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PertanyaanYelyelRequest {

    KriteriaYelyel kriteria;
    List<PointPenilaianYelyel> points;

}

