package com.ms.springms.repository.penjurian.lapangan;

import com.ms.springms.entity.penjurian.lapangan.PertanyaanLapangan;
import com.ms.springms.entity.penjurian.lapangan.PointLapangan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointLapanganRepository extends JpaRepository<PointLapangan, Long> {
    List<PointLapangan> findByPertanyaanLapangan(PertanyaanLapangan pertanyaanLapangan);

    List<PointLapangan> findByPertanyaanLapanganAndActiveTrue(PertanyaanLapangan pertanyaanLapangan);

}
