package com.ms.springms.service.penjurian.presentasi;

import com.ms.springms.entity.Event;
import com.ms.springms.entity.Team;
import com.ms.springms.entity.UserInfo;
import com.ms.springms.entity.penjurian.lapangan.DetailEvaluasiLapangan;
import com.ms.springms.entity.penjurian.presentasi.DetailEvaluasiPresentasi;
import com.ms.springms.entity.penjurian.presentasi.PertanyaanPresentasi;
import com.ms.springms.model.penjurian.HasilEvaluasiDTO;
import com.ms.springms.model.penjurian.presentasi.DetailEvaluasiPresentasiDTO;
import com.ms.springms.model.penjurian.presentasi.EvaluasiPresentasiDTO;
import com.ms.springms.model.utils.PageResponse;
import com.ms.springms.repository.event.EventRepository;
import com.ms.springms.repository.penjurian.presentasi.DetailEvaluasiPresentasiRepository;
import com.ms.springms.repository.penjurian.presentasi.PertanyaanPresentasiRepository;
import com.ms.springms.repository.team.TeamRepository;
import com.ms.springms.repository.user.UserRepository;
import com.ms.springms.service.penjurian.TotalPenilaianService;
import com.ms.springms.utils.Exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DetailEvaluasiPresentasiService {

    @Autowired
    private DetailEvaluasiPresentasiRepository detailEvaluasiPresentasiRepository;

    @Autowired
    private TotalPenilaianService totalPenilaianService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private PertanyaanPresentasiRepository pertanyaanPresentasiRepository;

    public List<DetailEvaluasiPresentasi> createEvaluations(List<DetailEvaluasiPresentasi> evaluations) {
        double totalScorePresentasi = 0.0;
        Long teamId = null;
        Long eventId = null;
        Long userId = null;

        // Ambil waktu sekarang sekali saja
        LocalDateTime createdAt = LocalDateTime.now();

        List<DetailEvaluasiPresentasi> tempEvaluations = new ArrayList<>();

        for (DetailEvaluasiPresentasi evaluation : evaluations) {
            evaluation.setCreatedAt(createdAt);
            totalScorePresentasi += Double.parseDouble(evaluation.getScore());

            teamId = evaluation.getTeamId();
            eventId = evaluation.getEventId();
            userId = evaluation.getUserId();

            tempEvaluations.add(evaluation);
        }

        List<DetailEvaluasiPresentasi> savedEvaluations = detailEvaluasiPresentasiRepository.saveAll(tempEvaluations);

        if (teamId != null && eventId != null && userId != null) {
            totalPenilaianService.updateScore(teamId, eventId, userId, totalScorePresentasi, "presentasi");
        }

        return savedEvaluations;
    }
    @Transactional
    public List<DetailEvaluasiPresentasi> updateEvaluasiPresentasi(List<DetailEvaluasiPresentasi> updatedEvaluasiList) {
        if (updatedEvaluasiList == null || updatedEvaluasiList.isEmpty()) {
            throw new IllegalArgumentException("Updated evaluasi list cannot be null or empty");
        }

        Long userId = updatedEvaluasiList.get(0).getUserId();
        Long eventId = updatedEvaluasiList.get(0).getEventId();
        Long teamId = updatedEvaluasiList.get(0).getTeamId();

        List<DetailEvaluasiPresentasi> existingEvaluasiList = detailEvaluasiPresentasiRepository.findByUserIdAndTeamIdAndEventId(userId, teamId, eventId);

        double totalScorePresentasi = 0.0;
        List<DetailEvaluasiPresentasi> savedEvaluasiList = new ArrayList<>();

        for (DetailEvaluasiPresentasi updatedEvaluasi : updatedEvaluasiList) {
            DetailEvaluasiPresentasi existingEvaluasi = detailEvaluasiPresentasiRepository.findById(updatedEvaluasi.getId())
                    .orElseThrow(() -> new EntityNotFoundException("DetailEvaluasiPresentasi not found"));

            existingEvaluasi.setScore(updatedEvaluasi.getScore());
            savedEvaluasiList.add(existingEvaluasi);
        }

        savedEvaluasiList = detailEvaluasiPresentasiRepository.saveAll(savedEvaluasiList);

        for (DetailEvaluasiPresentasi evaluasi : existingEvaluasiList) {
            totalScorePresentasi += Double.parseDouble(evaluasi.getScore());
        }

        totalPenilaianService.updateScore(teamId, eventId, userId, totalScorePresentasi, "presentasi");

        return savedEvaluasiList;
    }
    public List<HasilEvaluasiDTO> getEvaluasiPresentasiByUser(Long userId , Long teamId , Long eventId){
        List<DetailEvaluasiPresentasi> detailPresentasi;

        detailPresentasi = detailEvaluasiPresentasiRepository.findByUserIdAndTeamIdAndEventId(userId,teamId,eventId);
        return detailPresentasi.stream().map(evaluasi -> {
            UserInfo user = userRepository.findById(userId).orElse(null);
            Team team  = teamRepository.findById(teamId).orElse(null);
            Event event = eventRepository.findById(eventId).orElse(null);
            PertanyaanPresentasi pertanyaanPresentasi = pertanyaanPresentasiRepository.findById(evaluasi.getPertanyaanId()).orElse(null);
            return new HasilEvaluasiDTO(
                    evaluasi.getId(),
                    evaluasi.getScore(),
                    evaluasi.getUserId(),
                    evaluasi.getPertanyaanId(),
                    team != null ? team.getTeamId() : null,
                    event != null ? event.getEventId() : null,
                    evaluasi.getCreatedAt(),
                    user != null ? user.getUsername() : null,
                    team != null ? team.getTeamName() : null,
                    event != null ? event.getEventName() : null,
                    pertanyaanPresentasi != null ? pertanyaanPresentasi.getPertanyaan() : null
            );
        }).collect(Collectors.toList());
    }


    public ResponseEntity<?> getDetailEvaluasi(String search, Pageable pageable) {
        try {
            // Panggil repository untuk mendapatkan data berdasarkan search term
            Page<EvaluasiPresentasiDTO> evaluasiPage = detailEvaluasiPresentasiRepository.findAllEvaluasiPresentasiBySearchTerm(search, pageable);

            // Buat PageResponse yang akan dikembalikan
            PageResponse<List<EvaluasiPresentasiDTO>> pageResponse = new PageResponse<>(
                    evaluasiPage.getContent(),
                    pageable.getPageNumber() + 1,
                    evaluasiPage.getTotalPages(),
                    pageable.getPageSize(),
                    evaluasiPage.getTotalElements()
            );

            // Jika data ditemukan, kembalikan response dengan status OK
            return new ResponseEntity<>(pageResponse, HttpStatus.OK);

        } catch (Exception e) {
            // Jika ada error, tangkap dan kembalikan pesan error dengan status INTERNAL_SERVER_ERROR
            return new ResponseEntity<>("Error occurred while fetching evaluation details: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Map<String, Object> getEvaluasiPresentasiAttributes() {
        List<DetailEvaluasiPresentasi> details = detailEvaluasiPresentasiRepository.findAll();

        // Gunakan Set untuk menghindari duplikasi
        Set<Map<String, Object>> teams = new HashSet<>();
        Set<Map<String, Object>> events = new HashSet<>();
        Set<Map<String, Object>> juris = new HashSet<>();
        Set<String> createdAts = new HashSet<>();

        // Proses data dari detail evaluasi
        for (DetailEvaluasiPresentasi detail : details) {
            Team team = teamRepository.findById(detail.getTeamId()).orElse(null);
            Event event = eventRepository.findById(detail.getEventId()).orElse(null);
            UserInfo juri = userRepository.findById(detail.getUserId()).orElse(null);

            if (team != null && event != null && juri != null) {
                Map<String, Object> teamMap = new HashMap<>();
                teamMap.put("id", detail.getTeamId());
                teamMap.put("name", team.getTeamName());
                teams.add(teamMap);

                Map<String, Object> eventMap = new HashMap<>();
                eventMap.put("id", detail.getEventId());
                eventMap.put("name", event.getEventName());
                events.add(eventMap);

                Map<String, Object> juriMap = new HashMap<>();
                juriMap.put("id", detail.getUserId());
                juriMap.put("name", juri.getUsername());
                juris.add(juriMap);

                createdAts.add(String.valueOf(detail.getCreatedAt().getYear()));
            }
        }

        // Buat map yang berisi list dari masing-masing atribut
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("teams", new ArrayList<>(teams));
        resultMap.put("events", new ArrayList<>(events));
        resultMap.put("juris", new ArrayList<>(juris));
        resultMap.put("createdAt", new ArrayList<>(createdAts));

        return resultMap;
    }

    public List<DetailEvaluasiPresentasiDTO> getDetailEvaluasi(Long teamId, Long userId, Long eventId) {
        List<Object[]> results = detailEvaluasiPresentasiRepository.findDetailEvaluasiByTeamIdAndUserIdAndEventIdAndCreatedAt(teamId, userId, eventId);

        if (results.isEmpty()) {
            throw new ResourceNotFoundException("Data tidak ditemukan untuk teamId: " + teamId);
        }

        return results.stream().map(result -> {
            DetailEvaluasiPresentasiDTO dto = new DetailEvaluasiPresentasiDTO();
            dto.setId((Long) result[0]);
            dto.setPertanyaanId((Long) result[1]);
            dto.setPertanyaan((String) result[2]);
            dto.setScore(Double.valueOf((String) result[3]));
            return dto;
        }).collect(Collectors.toList());
    }

    public void updateScorePresentasi(List<DetailEvaluasiPresentasi> evaluations) {
        for (DetailEvaluasiPresentasi evaluation : evaluations) {
            DetailEvaluasiPresentasi existingEvaluation = detailEvaluasiPresentasiRepository.findById(evaluation.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid evaluation ID: " + evaluation.getId()));

            existingEvaluation.setScore(evaluation.getScore().toString());

            detailEvaluasiPresentasiRepository.save(existingEvaluation);
        }
    }

}