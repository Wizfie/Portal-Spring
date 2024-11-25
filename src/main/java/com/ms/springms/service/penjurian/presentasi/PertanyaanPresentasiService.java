package com.ms.springms.service.penjurian.presentasi;

import com.ms.springms.entity.penjurian.lapangan.KriteriaLapangan;
import com.ms.springms.entity.penjurian.presentasi.PertanyaanPresentasi;
import com.ms.springms.entity.penjurian.presentasi.PointPresentasi;
import com.ms.springms.model.penjurian.presentasi.PertanyaanAndPoint;
import com.ms.springms.model.penjurian.presentasi.PointPresentasiDTO;
import com.ms.springms.repository.penjurian.presentasi.PertanyaanPresentasiRepository;
import com.ms.springms.repository.penjurian.presentasi.PointPresentasiRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PertanyaanPresentasiService {

    @Autowired
    private PertanyaanPresentasiRepository pertanyaanPresentasiRepository;

    @Autowired
    private PointPresentasiRepository pointPresentasiRepository;

    @Transactional
    public PertanyaanPresentasi savePertanyaanWithPoints(PertanyaanPresentasi pertanyaanPresentasi, List<PointPresentasi> points) {
        PertanyaanPresentasi savedPertanyaan;

        if (pertanyaanPresentasi.getId() != null) {
            savedPertanyaan = pertanyaanPresentasiRepository.findById(pertanyaanPresentasi.getId())
                    .orElseThrow(() -> new IllegalArgumentException("PertanyaanPresentasi exists: "));
        } else {
            pertanyaanPresentasi.setActive(true);
            savedPertanyaan = pertanyaanPresentasiRepository.save(pertanyaanPresentasi);
        }

        for (PointPresentasi point : points) {
            point.setPertanyaanPresentasi(savedPertanyaan);
            point.setActive(savedPertanyaan.getActive());
            pointPresentasiRepository.save(point);
        }

        return savedPertanyaan;
    }


    public List<PertanyaanAndPoint> getAllPertanyaanWithPoints(String type) {
        List<PertanyaanPresentasi> pertanyaanList;

        if (type != null) {
            pertanyaanList = pertanyaanPresentasiRepository.findByTypeAndActiveTrue(type);
        } else {
            pertanyaanList = pertanyaanPresentasiRepository.findAll()   ;
        }

        return pertanyaanList.stream().map(pertanyaan -> {
            // Ambil semua point berdasarkan pertanyaan
            List<PointPresentasi> points = pointPresentasiRepository.findByPertanyaanPresentasi(pertanyaan);

            // Konversi list of PointPresentasi ke list of PointPresentasiDTO
            List<PointPresentasiDTO> pointsDTO = points.stream().map(point -> {
                return new PointPresentasiDTO(point.getId(), point.getPointPenilaian(), point.getScoreMaksimal() , point.getActive());
            }).collect(Collectors.toList());

            // Kembalikan PertanyaanAndPoint menggunakan DTO
            return new PertanyaanAndPoint(pertanyaan.getId(), pertanyaan.getPertanyaan(), pertanyaan.getType() ,pertanyaan.getActive(), pointsDTO);
        }).collect(Collectors.toList());
    }


    public void updatePertanyaan(Long id, String pertanyaan, Boolean active) {
        Optional<PertanyaanPresentasi> optionalPertanyaanPresentasi = pertanyaanPresentasiRepository.findById(id);

        if (optionalPertanyaanPresentasi.isPresent()) {
            PertanyaanPresentasi pertanyaanPresentasi = optionalPertanyaanPresentasi.get();

            // Update pertanyaan hanya jika ada nilai baru
            if (pertanyaan != null) {
                pertanyaanPresentasi.setPertanyaan(pertanyaan);
            }

            // Update status active hanya jika ada nilai baru
            if (active != null) {
                pertanyaanPresentasi.setActive(active);

                // Jika status active berubah, update Point yang terkait
                List<PointPresentasi> points = pointPresentasiRepository.findByPertanyaanPresentasi(pertanyaanPresentasi);
                for (PointPresentasi point : points) {
                    point.setActive(active);  // Mengikuti status pertanyaan
                    pointPresentasiRepository.save(point);
                }
            }

            pertanyaanPresentasiRepository.save(pertanyaanPresentasi);
        } else {
            throw new RuntimeException("Pertanyaan Presentasi not found");
        }
    }


    public void updatePointPenilaian(Long id, String pointPenilaian, String scoreMaksimal, Boolean active) {
        Optional<PointPresentasi> optionalPointPresentasi = pointPresentasiRepository.findById(id);

        if (optionalPointPresentasi.isPresent()) {
            PointPresentasi pointPresentasi = optionalPointPresentasi.get();

            // Update pointPenilaian hanya jika ada nilai baru
            if (pointPenilaian != null) {
                pointPresentasi.setPointPenilaian(pointPenilaian);
            }

            // Update scoreMaksimal hanya jika ada nilai baru
            if (scoreMaksimal != null) {
                pointPresentasi.setScoreMaksimal(scoreMaksimal);
            }

            // Update status active hanya jika ada nilai baru
            if (active != null) {
                pointPresentasi.setActive(active);
            }

            pointPresentasiRepository.save(pointPresentasi);
        } else {
            throw new RuntimeException("Point Penilaian Presentasi not found");
        }
    }




}

