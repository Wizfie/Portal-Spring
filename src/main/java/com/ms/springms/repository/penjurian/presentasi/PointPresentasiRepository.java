package com.ms.springms.repository.penjurian.presentasi;

import com.ms.springms.entity.penjurian.presentasi.PertanyaanPresentasi;
import com.ms.springms.entity.penjurian.presentasi.PointPresentasi;
import com.ms.springms.model.penjurian.presentasi.PointPresentasiDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointPresentasiRepository extends JpaRepository<PointPresentasi, Long> {
    List<PointPresentasi> findByPertanyaanPresentasi(PertanyaanPresentasi pertanyaan);


}
