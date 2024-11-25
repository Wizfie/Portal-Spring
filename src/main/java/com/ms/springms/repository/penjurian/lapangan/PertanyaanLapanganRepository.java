package com.ms.springms.repository.penjurian.lapangan;

import com.ms.springms.entity.penjurian.lapangan.PertanyaanLapangan;
import com.ms.springms.entity.penjurian.lapangan.SubKriteriaLapangan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PertanyaanLapanganRepository  extends JpaRepository<PertanyaanLapangan , Long> {



    boolean existsByPertanyaanAndSubKriteriaLapangan(String pertanyaan, SubKriteriaLapangan subKriteriaLapangan);

    List<PertanyaanLapangan> findBySubKriteriaLapangan(SubKriteriaLapangan subKriteriaLapangan);

    List<PertanyaanLapangan> findBySubKriteriaLapanganAndActiveTrue(SubKriteriaLapangan subKriteriaLapangan);

    @Query("SELECT p FROM PertanyaanLapangan p WHERE p.id IN :pertanyaanIds")
    List<PertanyaanLapangan> findByIdIn(@Param("pertanyaanIds") List<Long> pertanyaanIds);

}
