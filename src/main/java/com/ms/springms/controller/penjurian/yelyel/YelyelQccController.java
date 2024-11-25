package com.ms.springms.controller.penjurian.yelyel;

import com.ms.springms.entity.penjurian.yelyel.DetailEvaluasiYelyel;
import com.ms.springms.entity.penjurian.yelyel.EvaluationYelyelDTO;
import com.ms.springms.entity.penjurian.yelyel.KriteriaYelyel;
import com.ms.springms.model.penjurian.yelyel.*;
import com.ms.springms.model.utils.PageResponse;
import com.ms.springms.service.penjurian.yelyel.YelyelQccService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/yelyel")
public class YelyelQccController {

    @Autowired
    private YelyelQccService yelyelService;

    @GetMapping("/pertanyaan-all")
    public List<PertanyaanYelyelDTO> getPertanyaanYelyel(@RequestParam(value = "activeOnly", defaultValue = "false") boolean activeOnly) {
        return yelyelService.getPertanyaanYelyel(activeOnly);
    }

    @PostMapping("/create-pertanyaan-qcc")
    public ResponseEntity<PertanyaanYelyelRequest> savePertanyaanWithPoints(@RequestBody PertanyaanYelyelRequest request) {
        KriteriaYelyel savedPertanyaan = yelyelService.savePertanyaanWithPoints(request.getKriteria(), request.getPoints());
        return new ResponseEntity<>(new PertanyaanYelyelRequest(savedPertanyaan, request.getPoints()), HttpStatus.OK);
    }

    @PostMapping("/create-evaluasi")
    public ResponseEntity<?> createEvaluations(@RequestBody List<DetailEvaluasiYelyel> evaluations) {
        try {
            String validationError = validateEvaluations(evaluations);
            if (validationError != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationError);
            }
            List<DetailEvaluasiYelyel> createdEvaluations = yelyelService.createEvaluations(evaluations);
            return ResponseEntity.ok(createdEvaluations);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create evaluations: " + ex.getMessage());
        }
    }

    @PutMapping("/{id}/kriteria")
    public ResponseEntity<String> updateKriteria(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean active) {

        yelyelService.updateKriteria(id, name, active);
        return ResponseEntity.ok("kriteria updated successfully.");
    }

    @PutMapping("/{id}/pertanyaan")
    public ResponseEntity<String> updateKriteria(
            @PathVariable Long id,
            @RequestParam(required = false) String pertanyaan,
            @RequestParam(required = false) String scoreMaksimal,
            @RequestParam(required = false) Boolean active) {

        yelyelService.updatePointPertanyaan(id, pertanyaan, scoreMaksimal, active);
        return ResponseEntity.ok("pertanyaan updated successfully.");
    }


    @GetMapping("/summary-scores")
    public ResponseEntity<?> getScoreSummariesByOptionalParams(
            @RequestParam(required = false) Long juriId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Integer year) {
        try {
            // Jika semua parameter null, ambil semua data (tidak ada filter)
            if (juriId == null && deptId == null && year == null) {
                List<ScoreSummary> allSummaries = yelyelService.getAllScoreSummaries();
                return new ResponseEntity<>(allSummaries, HttpStatus.OK);
            }

            // Jika ada parameter yang diisi, lakukan query berdasarkan param yang tersedia
            List<ScoreSummary> filteredSummaries = yelyelService.getScoreSummariesByParams(juriId, deptId, year);
            return new ResponseEntity<>(filteredSummaries, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching score summaries: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/details-evaluasi")
    public ResponseEntity<List<DetailEvaluasiYelyelDTO>> getEvaluasiByDeptIdAndJuriIdAndYear(
            @RequestParam Long deptId,
            @RequestParam Long juriId,
            @RequestParam int year) {
        try {
            List<DetailEvaluasiYelyelDTO> result = yelyelService.getEvaluasiByDeptIdAndJuriIdAndYear(deptId, juriId, year);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }


    @GetMapping("/evaluasi-by-penilai")
    public ResponseEntity<?> getEvaluasiYelyelByUser(@RequestParam Long userId, @RequestParam Long juriId, @RequestParam int year) {
        try {
            List<HasilEvaluasiYelyelDTO> hasil = yelyelService.getEvaluasiYelyelByUser(userId, juriId, year);
            return ResponseEntity.ok(hasil);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Terjadi kesalahan pada server.");
        }
    }

    @GetMapping("/evaluations-list")
    public ResponseEntity<?> searchEvaluations(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            PageResponse<List<EvaluationYelyelDTO>> response = yelyelService.evaluationList(search, page, size);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/final-scores")
    public ResponseEntity<?> getAllDeptScoresWithJuriAndYear(@RequestParam(required = false) Integer year) {
        try {
            // Memanggil service untuk mengambil data berdasarkan tahun
            List<DeptScoreDTO> results = yelyelService.getAllDeptScoresWithJuriAndYear(year);

            return new ResponseEntity<>(results, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/attributes")
    public ResponseEntity<?> getYelyelAttributes() {
        try {
            Map<String, Object> detailAttributes = yelyelService.getEvaluasiLapanganAttributes();
            return ResponseEntity.ok(detailAttributes);
        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)  // 500 Internal Server Error
                    .body("Error retrieving detail evaluasi attributes: " + e.getMessage());
        }
    }


    @GetMapping("/detail-evaluation")
    public ResponseEntity<?> getEvaluasiByDeptJuriAndCreatedAt(
            @RequestParam("deptId") Long deptId,
            @RequestParam("juriId") Long juriId,
            @RequestParam("createdAt") LocalDateTime createdAt) {

        try {
            List<EvaluasiResponse> evaluasiResponse = yelyelService.getEvaluasiByDeptJuriAndCreatedAt(deptId, juriId, createdAt);
            return new ResponseEntity<>(evaluasiResponse, HttpStatus.OK);
        } catch (RuntimeException e) {
            // Handle runtime exception (e.g., data not found or any other error)
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            // Handle general exceptions
            return new ResponseEntity<>("Unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update-scores")
    public ResponseEntity<?> updateMultipleScores(@RequestBody List<DetailEvaluasiYelyel> evaluations) {
        try {
            yelyelService.updateScoreYelyel(evaluations);
            return ResponseEntity.ok("Scores updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating scores");
        }
    }



// Helper

    private String validateEvaluations(List<DetailEvaluasiYelyel> evaluations) {
        if (evaluations == null || evaluations.isEmpty()) {
            return "Evaluation list cannot be null or empty";
        }
        for (DetailEvaluasiYelyel evaluation : evaluations) {
            if (evaluation.getPertanyaanId() == null) {
                return "PertanyaanId cannot be null";
            }
            if (evaluation.getScore() == null || evaluation.getScore().toString().trim().isEmpty()) {
                return "Score cannot be null or empty";
            }
            if (evaluation.getDeptName() == null) {
                return "Dept Name cannot be null";
            }
            if (evaluation.getDeptId() == null) {
                return "Dept Id cannot be null";
            }
        }
        return null;
    }




}

