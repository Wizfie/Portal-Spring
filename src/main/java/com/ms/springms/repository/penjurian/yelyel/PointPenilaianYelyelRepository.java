package com.ms.springms.repository.penjurian.yelyel;

import com.ms.springms.entity.penjurian.yelyel.KriteriaYelyel;
import com.ms.springms.entity.penjurian.yelyel.PointPenilaianYelyel;
import com.ms.springms.model.penjurian.yelyel.PointYelyelDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointPenilaianYelyelRepository extends JpaRepository<PointPenilaianYelyel, Long> {
    List<PointPenilaianYelyel> findByKriteriaYelyelAndActiveTrue(KriteriaYelyel kriteriaYelyel);

    List<PointPenilaianYelyel> findByKriteriaYelyel(KriteriaYelyel kriteriaYelyel);
}
