package com.ms.springms.controller.penjurian;

import com.ms.springms.entity.TotalPenilaian;
import com.ms.springms.model.penjurian.TotalEvaluasiDTO;
import com.ms.springms.model.penjurian.UpdateScoreRequest;
import com.ms.springms.model.penjurian.totalScore.TeamScoreDTO;
import com.ms.springms.model.utils.PageResponse;
import com.ms.springms.service.penjurian.TotalPenilaianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/evaluasi")
public class TotalScoreController {

    @Autowired
    private TotalPenilaianService penilaianService;

    @GetMapping("/score-all")
    public PageResponse<List<TotalEvaluasiDTO>> searchEvaluasi(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String type) {

        // Validasi untuk memastikan bahwa startDate dan endDate diberikan bersamaan
        if ((startDate != null && endDate == null) || (startDate == null && endDate != null)) {
            throw new IllegalArgumentException("startDate dan endDate harus diberikan bersamaan.");
        }

        // Memanggil service untuk mengambil data
        return penilaianService.getScoreTotal(page, size, search, startDate, endDate, type);
    }


    @GetMapping("/final")
    public List<TeamScoreDTO> getFinalScore() {
        return penilaianService.getFinalScore();
    }

    @PutMapping("/{id}/update-score")
    public ResponseEntity<TotalPenilaian> updateScore(
            @PathVariable Long id,
            @RequestBody UpdateScoreRequest updateScoreRequest) {

        Optional<TotalPenilaian> updatedTotalPenilaian = penilaianService.updateScoreFinal(id, updateScoreRequest);

        return updatedTotalPenilaian
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}