package com.ms.springms.repository.penjurian.presentasi;

import com.ms.springms.entity.penjurian.lapangan.PertanyaanLapangan;
import com.ms.springms.entity.penjurian.presentasi.PertanyaanPresentasi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PertanyaanPresentasiRepository  extends JpaRepository<PertanyaanPresentasi, Long> {
    List<PertanyaanPresentasi> findByType(String type);

    Optional<PertanyaanPresentasi> findById(Long id);

    List<PertanyaanPresentasi> findByTypeAndActiveTrue(String type);

    List<PertanyaanPresentasi> findByActiveTrue();

    @Query("SELECT p FROM PertanyaanPresentasi p WHERE p.id IN :pertanyaanIds")
    List<PertanyaanPresentasi> findByIdIn(@Param("pertanyaanIds") List<Long> pertanyaanIds);
}
