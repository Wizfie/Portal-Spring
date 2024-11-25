package com.ms.springms.repository.penjurian.lapangan;

import com.ms.springms.entity.penjurian.lapangan.FaseLapangan;
import com.ms.springms.entity.penjurian.lapangan.KriteriaLapangan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KriteriaRepository extends JpaRepository<KriteriaLapangan, Long> {

    KriteriaLapangan findByName(String name);


    boolean existsByNameAndFaseLapangan(String name, FaseLapangan faseLapangan);

    List<KriteriaLapangan> findByFaseLapangan(FaseLapangan faseLapangan);


}
