package com.ms.springms.controller.penjurian.presentasi;

import com.ms.springms.entity.penjurian.lapangan.DetailEvaluasiLapangan;
import com.ms.springms.entity.penjurian.presentasi.DetailEvaluasiPresentasi;
import com.ms.springms.model.penjurian.HasilEvaluasiDTO;
import com.ms.springms.model.penjurian.presentasi.DetailEvaluasiPresentasiDTO;
import com.ms.springms.service.penjurian.presentasi.DetailEvaluasiPresentasiService;
import com.ms.springms.utils.Exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/presentasi")
public class DetailEvaluasiPresentasiController {

// Dependency injection
    @Autowired
    private DetailEvaluasiPresentasiService detailEvaluasiPresentasiService;

// API endpoints

    @PostMapping("/create-evaluasi")
    public ResponseEntity<?> createEvaluations(@RequestBody List<DetailEvaluasiPresentasi> evaluations) {
        try {
            String validationError = validateEvaluations(evaluations);
            if (validationError != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationError);
            }
            List<DetailEvaluasiPresentasi> createdEvaluations = detailEvaluasiPresentasiService.createEvaluations(evaluations);
            return ResponseEntity.ok(createdEvaluations);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create evaluations: " + ex.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<List<DetailEvaluasiPresentasi>> updateEvaluasiPresentasi(@RequestBody List<DetailEvaluasiPresentasi> updatedEvaluasiList) {
        try {
            List<DetailEvaluasiPresentasi> updatedEvaluasi = detailEvaluasiPresentasiService.updateEvaluasiPresentasi(updatedEvaluasiList);
            return ResponseEntity.ok(updatedEvaluasi);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/evaluasi-by-penilai")
    public List<HasilEvaluasiDTO> getEvaluasiPresentasiByUser(@RequestParam Long userId ,@RequestParam Long teamId , @RequestParam Long eventId ){
        return detailEvaluasiPresentasiService.getEvaluasiPresentasiByUser(userId,teamId,eventId);
}

    @GetMapping("/evaluation-list")
    public ResponseEntity<?> getDetailEvaluasiPresentasi(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            // (halaman mulai dari 0)
            Pageable pageable = PageRequest.of(page - 1, size);

            return detailEvaluasiPresentasiService.getDetailEvaluasi(search, pageable);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching data: " + e.getMessage());
        }
    }

    @GetMapping("/attributes")
    public ResponseEntity<?> getDetailEvaluasiAttributes() {
        try {
            Map<String, Object> detailAttributes = detailEvaluasiPresentasiService.getEvaluasiPresentasiAttributes();
            return ResponseEntity.ok(detailAttributes);  // 200 OK dengan data
        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)  // 500 Internal Server Error
                    .body("Error retrieving detail evaluasi attributes: " + e.getMessage());
        }
    }
    @GetMapping("/details-evaluation")
    public ResponseEntity<?> getDetailEvaluasi(
            @RequestParam Long teamId,
            @RequestParam Long userId,
            @RequestParam Long eventId
            ) {

        try {

            // Call service to get evaluation details
            List<DetailEvaluasiPresentasiDTO> detailEvaluasi = detailEvaluasiPresentasiService.getDetailEvaluasi(teamId, userId, eventId);

            return new ResponseEntity<>(detailEvaluasi, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>("Data tidak ditemukan", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Terjadi kesalahan: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping("/update-scores")
    public ResponseEntity<?> updateMultipleScores(@RequestBody List<DetailEvaluasiPresentasi> evaluations) {
        try {
            detailEvaluasiPresentasiService.updateScorePresentasi(evaluations);
            return ResponseEntity.ok("Scores updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating scores");
        }
    }

// Helper methods
    private String validateEvaluations(List<DetailEvaluasiPresentasi> evaluations) {
        if (evaluations == null || evaluations.isEmpty()) {
            return "Evaluation list cannot be null or empty";
        }
        for (DetailEvaluasiPresentasi evaluation : evaluations) {
            if (evaluation.getPertanyaanId() == null) {
                return "PertanyaanId cannot be null";
            }
            if (evaluation.getScore() == null || evaluation.getScore().toString().trim().isEmpty()) {
                return "Score cannot be null or empty";
            }
            if (evaluation.getTeamId() == null) {
                return "TeamId cannot be null";
            }
            if (evaluation.getEventId() == null) {
                return "EventId cannot be null";
            }
        }
        return null;
    }


}

