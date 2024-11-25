package com.ms.springms.repository.penjurian.lapangan;

import com.ms.springms.entity.penjurian.lapangan.KriteriaLapangan;
import com.ms.springms.entity.penjurian.lapangan.SubKriteriaLapangan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubKriteriaRepository extends JpaRepository<SubKriteriaLapangan, Long> {




    List<SubKriteriaLapangan> findByKriteriaLapangan(KriteriaLapangan kriteriaLapangan);

    boolean existsByNameAndKriteriaLapangan(String name, KriteriaLapangan kriteriaLapangan);

    Page<SubKriteriaLapangan> findByKriteriaLapangan_FaseLapangan_TypeAndActiveTrue(String type, Pageable pageable);
}
