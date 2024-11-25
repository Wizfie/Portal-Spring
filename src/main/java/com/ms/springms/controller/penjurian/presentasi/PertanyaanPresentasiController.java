package com.ms.springms.controller.penjurian.presentasi;

import com.ms.springms.entity.penjurian.presentasi.PertanyaanPresentasi;
import com.ms.springms.entity.penjurian.presentasi.PointPresentasi;
import com.ms.springms.model.penjurian.presentasi.PertanyaanAndPoint;
import com.ms.springms.model.penjurian.presentasi.PertanyaanWithPointRequest;
import com.ms.springms.model.penjurian.presentasi.UpdatePointRequest;
import com.ms.springms.service.penjurian.presentasi.PertanyaanPresentasiService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/presentasi")
public class PertanyaanPresentasiController {

    @Autowired
    private PertanyaanPresentasiService pertanyaanPresentasiService;

    @GetMapping("/pertanyaan-all")
    public List<PertanyaanAndPoint> getAllPertanyaanWithPoints(@RequestParam(required = false) String type) {
        return pertanyaanPresentasiService.getAllPertanyaanWithPoints(type);
    }

    @PostMapping("/create-pertanyaan")
    public ResponseEntity<PertanyaanWithPointRequest> savePertanyaanWithPoints(@RequestBody PertanyaanWithPointRequest request) {
        PertanyaanPresentasi savedPertanyaan = pertanyaanPresentasiService.savePertanyaanWithPoints(request.getPertanyaan(), request.getPoints());
        return new ResponseEntity<>(new PertanyaanWithPointRequest(savedPertanyaan, request.getPoints()), HttpStatus.OK);
    }

    // Update pertanyaan
    @PutMapping("/{id}/pertanyaan")
    public ResponseEntity<String> updatePertanyaan(
            @PathVariable Long id,
            @RequestParam(required = false) String pertanyaan,
            @RequestParam(required = false) Boolean active) {

        pertanyaanPresentasiService.updatePertanyaan(id, pertanyaan, active);
        return ResponseEntity.ok("Pertanyaan updated successfully.");
    }

    // Update point penilaian
    @PutMapping("/{id}/point")
    public ResponseEntity<String> updatePoint(
            @PathVariable Long id,
            @RequestParam(required = false) String pointPenilaian,
            @RequestParam(required = false) String scoreMaksimal,
            @RequestParam(required = false) Boolean active) {

        pertanyaanPresentasiService.updatePointPenilaian(id, pointPenilaian, scoreMaksimal, active);
        return ResponseEntity.ok("Point updated successfully.");
    }
}

