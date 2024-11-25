package com.ms.springms.service.penjurian.lapangan;

import com.ms.springms.entity.Event;
import com.ms.springms.entity.Team;
import com.ms.springms.entity.UserInfo;
import com.ms.springms.entity.penjurian.lapangan.*;
import com.ms.springms.entity.penjurian.lapangan.lapangan.*;
import com.ms.springms.model.penjurian.HasilEvaluasiDTO;
import com.ms.springms.model.penjurian.lapangan.DetailEvaluasiLapanganDTO;
import com.ms.springms.model.penjurian.lapangan.EvaluasiLapanganDTO;
import com.ms.springms.model.utils.PageResponse;
import com.ms.springms.repository.event.EventRepository;
import com.ms.springms.repository.penjurian.lapangan.*;
import com.ms.springms.repository.team.TeamRepository;
import com.ms.springms.repository.user.UserRepository;
import com.ms.springms.service.penjurian.TotalPenilaianService;
import com.ms.springms.utils.Exceptions.DuplicateEntryException;
import com.ms.springms.utils.Exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LapanganService {

    @Autowired
    private FaseRepository faseRepository;

    @Autowired
    private KriteriaRepository kriteriaRepository;

    @Autowired
    private SubKriteriaRepository subKriteriaRepository;

    @Autowired
    private PertanyaanLapanganRepository pertanyaanLapanganRepository;
    @Autowired
    private PointLapanganRepository pointLapanganRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private DetailEvaluasiLapanganRepository detailEvaluasiLapanganRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TotalPenilaianService totalPenilaianService;


    @Transactional
    public FaseLapangan createFase(FaseLapangan faseLapangan) {
        // Check for duplicate FaseLapangan
        if (faseRepository.existsByNameAndType(faseLapangan.getName() , faseLapangan.getType())) {
            throw new DuplicateEntryException("FaseLapangan with this name already exists");
        }
        faseLapangan.setActive(true);
        return faseRepository.save(faseLapangan);
    }

    public KriteriaLapangan addKriteriaToPhase(KriteriaDTO kriteriaDTO) {
        FaseLapangan faseLapangan = faseRepository.findById(kriteriaDTO.getFaseId()).orElseThrow(() -> new EntityNotFoundException("FaseLapangan not found"));
        // Check for duplicate KriteriaLapangan
        if (kriteriaRepository.existsByNameAndFaseLapangan(kriteriaDTO.getName(), faseLapangan)) {
            throw new DuplicateEntryException("KriteriaLapangan with this name already exists in this FaseLapangan");
        }
        KriteriaLapangan kriteriaLapangan = new KriteriaLapangan();
        kriteriaLapangan.setName(kriteriaDTO.getName());
        kriteriaLapangan.setActive(true);
        kriteriaLapangan.setFaseLapangan(faseLapangan);

        return kriteriaRepository.save(kriteriaLapangan);
    }

    public SubKriteriaLapangan addSubKriteriaToKriteria(SubKriteriaDTO subKriteriaDTO) {
        KriteriaLapangan kriteriaLapangan = kriteriaRepository.findById(subKriteriaDTO.getKriteriaId()).orElseThrow(() -> new EntityNotFoundException("KriteriaLapangan not found"));
        // Check for duplicate SubKriteriaLapangan
        if (subKriteriaRepository.existsByNameAndKriteriaLapangan(subKriteriaDTO.getName(), kriteriaLapangan)) {
            throw new DuplicateEntryException("SubKriteriaLapangan with this name already exists in this KriteriaLapangan");
        }
        SubKriteriaLapangan subKriteriaLapangan = new SubKriteriaLapangan();
        subKriteriaLapangan.setName(subKriteriaDTO.getName());
        subKriteriaLapangan.setActive(true);
        subKriteriaLapangan.setKriteriaLapangan(kriteriaLapangan);
        return subKriteriaRepository.save(subKriteriaLapangan);
    }

    public PertanyaanDTO createPertanyaanDanJawaban(PertanyaanDTO request) {
        // Mencari SubKriteriaLapangan berdasarkan ID yang diberikan
        SubKriteriaLapangan subKriteriaLapangan = subKriteriaRepository.findById(request.getSubKriteriaId())
                .orElseThrow(() -> new EntityNotFoundException("SubKriteriaLapangan not found"));
        if (pertanyaanLapanganRepository.existsByPertanyaanAndSubKriteriaLapangan(request.getPertanyaan() , subKriteriaLapangan)) {
            throw new DuplicateEntryException("Pertanyaan with this name already exists in this SubKriteriaLapangan");
        }

        // Membuat objek PertanyaanLapangan dari data request
        PertanyaanLapangan pertanyaanLapangan = new PertanyaanLapangan();
        pertanyaanLapangan.setPertanyaan(request.getPertanyaan());
        pertanyaanLapangan.setActive(true);
        pertanyaanLapangan.setSubKriteriaLapangan(subKriteriaLapangan);

        // Menyimpan PertanyaanLapangan ke database
        pertanyaanLapangan = pertanyaanLapanganRepository.save(pertanyaanLapangan);

        // Membuat objek PointLapangan untuk setiap jawaban dari data request
        List<String> jawabanList = request.getJawabanList();
        List<PointLapangan> pointLapanganList = new ArrayList<>();
        for (String jawaban : jawabanList) {
            PointLapangan pointLapangan = new PointLapangan();
            pointLapangan.setJawaban(jawaban);
            pointLapangan.setActive(true);
            pointLapangan.setPertanyaanLapangan(pertanyaanLapangan);
            pointLapanganList.add(pointLapangan);
        }

        // Menyimpan semua PointLapangan ke database
        pointLapanganRepository.saveAll(pointLapanganList);

        // Membuat DTO untuk respons
        PertanyaanDTO pertanyaanDTO = new PertanyaanDTO();
        pertanyaanDTO.setId(pertanyaanLapangan.getId());
        pertanyaanDTO.setPertanyaan(pertanyaanLapangan.getPertanyaan());
        pertanyaanDTO.setJawabanList(jawabanList);

        return pertanyaanDTO;
    }

    public PageResponse<List<SubKriteriaDTO>> getSubKriteriaByTypeWithPertanyaanAndJawaban(String type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SubKriteriaLapangan> subKriteriaPage = subKriteriaRepository.findByKriteriaLapangan_FaseLapangan_TypeAndActiveTrue(type, pageable);

        List<SubKriteriaDTO> subKriteriaDTOs = subKriteriaPage.getContent().stream()
                .map(this::mapToSubKriteriaDTO)
                .collect(Collectors.toList());

        return new PageResponse<>(
                subKriteriaDTOs,
                subKriteriaPage.getNumber(),
                subKriteriaPage.getTotalPages(),
                subKriteriaPage.getSize(),
                subKriteriaPage.getTotalElements()
        );
    }

    private SubKriteriaDTO mapToSubKriteriaDTO(SubKriteriaLapangan subKriteriaLapangan) {
        SubKriteriaDTO dto = new SubKriteriaDTO();
        dto.setId(subKriteriaLapangan.getId());
        dto.setName(subKriteriaLapangan.getName());
        dto.setKriteriaId(subKriteriaLapangan.getKriteriaLapangan().getId());
        dto.setPertanyaanList(subKriteriaLapangan.getPertanyaanLapanganList().stream()
                .map(this::mapToPertanyaanDTO)
                .collect(Collectors.toList()));

        // Set Fase Name and Type
        FaseLapangan fase = subKriteriaLapangan.getKriteriaLapangan().getFaseLapangan();
        if (fase != null) {
            dto.setFaseId(fase.getId());
            dto.setFaseName(fase.getName());
            dto.setFaseType(fase.getType());
        }
        return dto;
    }

    private PertanyaanDTO mapToPertanyaanDTO(PertanyaanLapangan pertanyaan) {
        PertanyaanDTO dto = new PertanyaanDTO();
        dto.setId(pertanyaan.getId());
        dto.setPertanyaan(pertanyaan.getPertanyaan());
        dto.setJawabanList(pertanyaan.getPoints().stream()
                .map(PointLapangan::getJawaban)
                .collect(Collectors.toList()));


        return dto;
    }
    public List<DetailEvaluasiLapangan> getAll() {
        return detailEvaluasiLapanganRepository.findAll();
    }
    @Transactional
    public List<DetailEvaluasiLapangan> saveAllEvaluasiLapangan(List<DetailEvaluasiLapangan> evaluasiList, Long teamId) {
        if (evaluasiList == null || evaluasiList.isEmpty()) {
            throw new IllegalArgumentException("Evaluasi list cannot be null or empty");
        }

        // Mengambil userId dan eventId dari elemen pertama dalam evaluasiList
        Long userId = evaluasiList.get(0).getUserId();
        Long eventId = evaluasiList.get(0).getEventId();
        LocalDate today = LocalDate.now();

        // Cek apakah sudah ada evaluasi untuk user, team, dan event pada hari ini
        List<DetailEvaluasiLapangan> existingEvaluasi = detailEvaluasiLapanganRepository
                .findByUserIdAndTeamIdAndEventIdAndDate(userId, teamId, eventId, today);
        if (!existingEvaluasi.isEmpty()) {
            throw new IllegalArgumentException("DetailEvaluasiLapangan data for the user and team already exists for today");
        }

        double totalScoreLapangan = 0.0;
        List<DetailEvaluasiLapangan> tempEvaluasiList = new ArrayList<>();

        // Mengambil waktu sekarang sekali saja untuk digunakan pada semua evaluasi
        LocalDateTime now = LocalDateTime.now();

        // Iterasi untuk menghitung total skor dan mempersiapkan evaluasi untuk disimpan
        for (DetailEvaluasiLapangan evaluasi : evaluasiList) {
            if (teamId != null && evaluasi.getScore() != null) {
                // Set createdAt dengan waktu yang sama dan teamId untuk semua evaluasi
                evaluasi.setCreatedAt(now);
                evaluasi.setTeamId(teamId);

                // Tambahkan skor ke total
                totalScoreLapangan += Double.parseDouble(evaluasi.getScore());

                // Tambahkan evaluasi ke list sementara
                tempEvaluasiList.add(evaluasi);
            } else {
                throw new IllegalArgumentException("TeamId or Score cannot be null");
            }
        }

        // Simpan semua evaluasi sekaligus dalam satu operasi saveAll
        List<DetailEvaluasiLapangan> savedEvaluasiList = detailEvaluasiLapanganRepository.saveAll(tempEvaluasiList);

        // Update total skor lapangan berdasarkan teamId, eventId, dan userId
        totalPenilaianService.updateScore(teamId, eventId, userId, totalScoreLapangan, "lapangan");

        return savedEvaluasiList;
    }

    @Transactional
    public List<DetailEvaluasiLapangan> updateEvaluasiLapangan(List<DetailEvaluasiLapangan> updatedEvaluasiList) {
        if (updatedEvaluasiList == null || updatedEvaluasiList.isEmpty()) {
            throw new IllegalArgumentException("Updated evaluasi list cannot be null or empty");
        }

        // Mengambil informasi user, event dan team dari updatedEvaluasiList pertama
        Long userId = updatedEvaluasiList.get(0).getUserId();
        Long eventId = updatedEvaluasiList.get(0).getEventId();
        Long teamId = updatedEvaluasiList.get(0).getTeamId();

        // Mengambil semua evaluasi yang ada untuk user, event, dan team yang relevan
        List<DetailEvaluasiLapangan> existingEvaluasiList = detailEvaluasiLapanganRepository.findByUserIdAndTeamIdAndEventId(userId, teamId, eventId);

        double totalScoreLapangan = 0.0;
        List<DetailEvaluasiLapangan> savedEvaluasiList = new ArrayList<>();

        // Update fields in the existing evaluasi
        for (DetailEvaluasiLapangan updatedEvaluasi : updatedEvaluasiList) {
            DetailEvaluasiLapangan existingEvaluasi = detailEvaluasiLapanganRepository.findById(updatedEvaluasi.getId())
                    .orElseThrow(() -> new EntityNotFoundException("DetailEvaluasiLapangan not found"));

            existingEvaluasi.setScore(updatedEvaluasi.getScore());
            savedEvaluasiList.add(existingEvaluasi);
        }

        // Save the updated evaluations
        savedEvaluasiList = detailEvaluasiLapanganRepository.saveAll(savedEvaluasiList);

        // Hitung total score dari semua evaluasi yang ada
        for (DetailEvaluasiLapangan evaluasi : existingEvaluasiList) {
            totalScoreLapangan += Double.parseDouble(evaluasi.getScore());
        }

        // Update the total score
        totalPenilaianService.updateScore(teamId, eventId, userId, totalScoreLapangan, "lapangan");

        return savedEvaluasiList;
    }

    public List<HasilEvaluasiDTO> getEvaluasiLapanganByUser
            (Long userId , Long teamId, Long eventId  ) {
        List<DetailEvaluasiLapangan> detailEvaluasiLapangans;

        detailEvaluasiLapangans = detailEvaluasiLapanganRepository.findByUserIdAndTeamIdAndEventId(userId, teamId, eventId);
        return  detailEvaluasiLapangans.stream().map(evaluasi -> {
            UserInfo userInfo = userRepository.findById(userId).orElse(null);
            Team team = teamRepository.findById(teamId).orElse(null);
            Event event = eventRepository.findById(eventId).orElse(null);
            PertanyaanLapangan pertanyaanLapangan = pertanyaanLapanganRepository.findById(evaluasi.getPertanyaanId()).orElse(null);
            return new HasilEvaluasiDTO(
                    evaluasi.getId(),
                    evaluasi.getScore(),
                    evaluasi.getUserId(),
                    evaluasi.getPertanyaanId(),
                    team != null ? team.getTeamId() : null,
                    event != null ? event.getEventId() : null,
                    evaluasi.getCreatedAt(),
                    userInfo != null ? userInfo.getUsername() : null,
                    team != null ? team.getTeamName() : null,
                    event != null ? event.getEventName() : null,
                    pertanyaanLapangan != null ? pertanyaanLapangan.getPertanyaan() : null
                  );
        }).collect(Collectors.toList());
    }

    public List<LapanganDataDTO> getAllLapanganData() {
        List<LapanganDataDTO> lapanganDataList = new ArrayList<>();

        // Get all Fases
        List<FaseLapangan> faseLapangans = faseRepository.findAll();
        for (FaseLapangan faseLapangan : faseLapangans) {
            LapanganDataDTO lapanganDataDTO = new LapanganDataDTO();
            lapanganDataDTO.setFaseId(faseLapangan.getId());
            lapanganDataDTO.setFaseName(faseLapangan.getName());
            lapanganDataDTO.setFaseType(faseLapangan.getType());


            // Get all Kriterias for the current FaseLapangan
            List<KriteriaLapangan> kriteriaLapangans = kriteriaRepository.findByFaseLapangan(faseLapangan);
            List<KriteriaDTO> kriteriaDTOList = new ArrayList<>();
            for (KriteriaLapangan kriteriaLapangan : kriteriaLapangans) {
                KriteriaDTO kriteriaDTO = new KriteriaDTO();
                kriteriaDTO.setId(kriteriaLapangan.getId());
                kriteriaDTO.setFaseId(faseLapangan.getId());
                kriteriaDTO.setName(kriteriaLapangan.getName());

                // Get all SubKriterias for the current KriteriaLapangan
                List<SubKriteriaLapangan> subKriteriaLapangans = subKriteriaRepository.findByKriteriaLapangan(kriteriaLapangan);
                List<SubKriteriaDTO> subKriteriaDTOList = new ArrayList<>();
                for (SubKriteriaLapangan subKriteriaLapangan : subKriteriaLapangans) {
                    SubKriteriaDTO subKriteriaDTO = new SubKriteriaDTO();
                    subKriteriaDTO.setId(subKriteriaLapangan.getId());
                    subKriteriaDTO.setName(subKriteriaLapangan.getName());
                    subKriteriaDTO.setKriteriaId(kriteriaLapangan.getId());

                    // Get all PertanyaanLapangans for the current SubKriteriaLapangan
                    List<PertanyaanLapangan> pertanyaanLapangans = pertanyaanLapanganRepository.findBySubKriteriaLapangan(subKriteriaLapangan);
                    List<PertanyaanDTO> pertanyaanDTOList = new ArrayList<>();
                    for (PertanyaanLapangan pertanyaanLapangan : pertanyaanLapangans) {
                        PertanyaanDTO pertanyaanDTO = new PertanyaanDTO();
                        pertanyaanDTO.setPertanyaan(pertanyaanLapangan.getPertanyaan());

                        // Get all Jawabans for the current PertanyaanLapangan
                        List<PointLapangan> pointLapangans = pointLapanganRepository.findByPertanyaanLapangan(pertanyaanLapangan);
                        List<String> jawabanList = new ArrayList<>();
                        for (PointLapangan pointLapangan : pointLapangans) {
                            jawabanList.add(pointLapangan.getJawaban());
                        }
                        pertanyaanDTO.setJawabanList(jawabanList);
                        pertanyaanDTOList.add(pertanyaanDTO);
                    }
                    subKriteriaDTO.setPertanyaanList(pertanyaanDTOList);
                    subKriteriaDTOList.add(subKriteriaDTO);
                }
                kriteriaDTO.setSubKriteriaList(subKriteriaDTOList);
                kriteriaDTOList.add(kriteriaDTO);
            }
            lapanganDataDTO.setKriteriaList(kriteriaDTOList);
            lapanganDataList.add(lapanganDataDTO);
        }
        return lapanganDataList;
    }


    public List<FaseLapanganDTO> getAllQuestions() {
        // Mengambil semua FaseLapangan yang aktif
        List<FaseLapangan> faseList = faseRepository.findAll();

        // Mengonversi setiap fase menjadi DTO
        return faseList.stream()
                .map(this::convertToFaseLapanganDTO)
                .collect(Collectors.toList());
    }

    private FaseLapanganDTO convertToFaseLapanganDTO(FaseLapangan fase) {
        // Ambil kriteria yang aktif untuk FaseLapangan ini
        List<KriteriaLapanganDTO> kriteriaDTOs = kriteriaRepository.findByFaseLapangan(fase).stream()
                .map(this::convertToKriteriaLapanganDTO)
                .collect(Collectors.toList());

        // Kembalikan DTO dengan kriteria yang aktif
        return new FaseLapanganDTO(fase.getId(), fase.getName(), fase.getType() ,fase.getActive(), kriteriaDTOs);
    }

    private KriteriaLapanganDTO convertToKriteriaLapanganDTO(KriteriaLapangan kriteria) {
        // Ambil subkriteria yang aktif untuk KriteriaLapangan ini
        List<SubKriteriaLapanganDTO> subKriteriaDTOs = subKriteriaRepository.findByKriteriaLapangan(kriteria).stream()
                .map(this::convertToSubKriteriaLapanganDTO)
                .collect(Collectors.toList());

        // Kembalikan DTO dengan subkriteria yang aktif
        return new KriteriaLapanganDTO(kriteria.getId(), kriteria.getName() ,kriteria.getActive(), subKriteriaDTOs);
    }

    private SubKriteriaLapanganDTO convertToSubKriteriaLapanganDTO(SubKriteriaLapangan subKriteria) {
        // Ambil pertanyaan yang aktif untuk SubKriteriaLapangan ini
        List<PertanyaanLapanganDTO> pertanyaanDTOs = pertanyaanLapanganRepository.findBySubKriteriaLapangan(subKriteria).stream()
                .map(this::convertToPertanyaanLapanganDTO)
                .collect(Collectors.toList());

        // Kembalikan DTO dengan pertanyaan yang aktif
        return new SubKriteriaLapanganDTO(subKriteria.getId(), subKriteria.getName(),subKriteria.getActive(), pertanyaanDTOs);
    }

    private PertanyaanLapanganDTO convertToPertanyaanLapanganDTO(PertanyaanLapangan pertanyaan) {
        // Ambil point yang aktif untuk PertanyaanLapangan ini
        List<PointLapanganDTO> pointDTOs = pointLapanganRepository.findByPertanyaanLapangan(pertanyaan).stream()
                .map(point -> new PointLapanganDTO(point.getId(), point.getJawaban(), point.getActive()))
                .collect(Collectors.toList());

        // Kembalikan DTO dengan point yang aktif
        return new PertanyaanLapanganDTO(pertanyaan.getId(), pertanyaan.getPertanyaan(), pertanyaan.getActive(), pointDTOs);
    }
    @Transactional
    public void updateFaseLapangan(Long faseId, String name, Boolean active) {
        FaseLapangan faseLapangan = faseRepository.findById(faseId)
                .orElseThrow(() -> new EntityNotFoundException("FaseLapangan not found"));

        // Update nama jika diberikan
        if (name != null && !name.trim().isEmpty()) {
            faseLapangan.setName(name);
        }

        // Update status active
        if (active != null) {
            faseLapangan.setActive(active);

            if (active) {
                // Jika mengaktifkan, propagasi ke bawah
                faseLapangan.getKriteriaLapangan().forEach(kriteria -> {
                    kriteria.setActive(true);
                    kriteria.getSubKriteriaLapangan().forEach(subKriteria -> {
                        subKriteria.setActive(true);
                        subKriteria.getPertanyaanLapanganList().forEach(pertanyaan -> {
                            pertanyaan.setActive(true);
                            pertanyaan.getPoints().forEach(point -> point.setActive(true));
                        });
                    });
                });
            } else {
                // Jika menonaktifkan, propagasi ke bawah
                faseLapangan.getKriteriaLapangan().forEach(kriteria -> {
                    kriteria.setActive(false);
                    kriteria.getSubKriteriaLapangan().forEach(subKriteria -> {
                        subKriteria.setActive(false);
                        subKriteria.getPertanyaanLapanganList().forEach(pertanyaan -> {
                            pertanyaan.setActive(false);
                            pertanyaan.getPoints().forEach(point -> point.setActive(false));
                        });
                    });
                });
            }
        }

        faseRepository.save(faseLapangan);
    }




    @Transactional
    public void updateKriteriaLapangan(Long kriteriaId, String name, Boolean active) {
        KriteriaLapangan kriteriaLapangan = kriteriaRepository.findById(kriteriaId)
                .orElseThrow(() -> new EntityNotFoundException("KriteriaLapangan not found"));

        // Update nama jika diberikan
        if (name != null && !name.trim().isEmpty()) {
            kriteriaLapangan.setName(name);
        }

        // Update status active
        if (active != null) {
            kriteriaLapangan.setActive(active);

            if (active) {
                // Jika mengaktifkan, propagasi ke bawah
                kriteriaLapangan.getSubKriteriaLapangan().forEach(subKriteria -> {
                    subKriteria.setActive(true);
                    subKriteria.getPertanyaanLapanganList().forEach(pertanyaan -> {
                        pertanyaan.setActive(true);
                        pertanyaan.getPoints().forEach(point -> point.setActive(true));
                    });
                });
            } else {
                // Jika menonaktifkan, propagasi ke bawah
                kriteriaLapangan.getSubKriteriaLapangan().forEach(subKriteria -> {
                    subKriteria.setActive(false);
                    subKriteria.getPertanyaanLapanganList().forEach(pertanyaan -> {
                        pertanyaan.setActive(false);
                        pertanyaan.getPoints().forEach(point -> point.setActive(false));
                    });
                });
            }
        }

        kriteriaRepository.save(kriteriaLapangan);
    }


    @Transactional
    public void updateSubKriteriaLapangan(Long subKriteriaId, String name, Boolean active) {
        SubKriteriaLapangan subKriteriaLapangan = subKriteriaRepository.findById(subKriteriaId)
                .orElseThrow(() -> new EntityNotFoundException("SubKriteriaLapangan not found"));

        // Update nama jika diberikan
        if (name != null && !name.trim().isEmpty()) {
            subKriteriaLapangan.setName(name);
        }

        // Update status active
        if (active != null) {
            subKriteriaLapangan.setActive(active);

            if (active) {
                // Jika mengaktifkan, propagasi ke bawah
                subKriteriaLapangan.getPertanyaanLapanganList().forEach(pertanyaan -> {
                    pertanyaan.setActive(true);
                    pertanyaan.getPoints().forEach(point -> point.setActive(true));
                });
            } else {
                // Jika menonaktifkan, propagasi ke bawah
                subKriteriaLapangan.getPertanyaanLapanganList().forEach(pertanyaan -> {
                    pertanyaan.setActive(false);
                    pertanyaan.getPoints().forEach(point -> point.setActive(false));
                });
            }
        }

        subKriteriaRepository.save(subKriteriaLapangan);
    }



    @Transactional
    public void updatePertanyaanLapangan(Long pertanyaanId, String name, Boolean active) {
        PertanyaanLapangan pertanyaanLapangan = pertanyaanLapanganRepository.findById(pertanyaanId)
                .orElseThrow(() -> new EntityNotFoundException("PertanyaanLapangan not found"));

        // Update nama jika diberikan
        if (name != null && !name.trim().isEmpty()) {
            pertanyaanLapangan.setPertanyaan(name);
        }

        // Update status active
        if (active != null) {
            pertanyaanLapangan.setActive(active);

            if (active) {
                // Jika mengaktifkan, propagasi ke bawah
                pertanyaanLapangan.getPoints().forEach(point -> point.setActive(true));
            } else {
                // Jika menonaktifkan, propagasi ke bawah
                pertanyaanLapangan.getPoints().forEach(point -> point.setActive(false));
            }
        }

        pertanyaanLapanganRepository.save(pertanyaanLapangan);
    }




    @Transactional
    public void updatePointLapangan(Long pointId, String name, Boolean active) {
        PointLapangan pointLapangan = pointLapanganRepository.findById(pointId)
                .orElseThrow(() -> new EntityNotFoundException("PointLapangan not found"));

        // Update nama jika diberikan
        if (name != null && !name.trim().isEmpty()) {
            pointLapangan.setJawaban(name);
        }

        // Update status active
        if (active != null) {
            pointLapangan.setActive(active);  // Tidak ada propagasi, karena ini level paling bawah
        }

        pointLapanganRepository.save(pointLapangan);
    }


    public Page<EvaluasiLapanganDTO> getDetailEvaluasiLapanganBySearchTerm(String searchTerm, Pageable pageable) {
        return detailEvaluasiLapanganRepository.findAllEvaluasiLapanganBySearchTerm(searchTerm, pageable);
    }

    public Map<String, Object> getEvaluasiLapanganAttributes() {
        List<DetailEvaluasiLapangan> details = detailEvaluasiLapanganRepository.findAll();

        // Gunakan Set untuk menghindari duplikasi
        Set<Map<String, Object>> teams = new HashSet<>();
        Set<Map<String, Object>> events = new HashSet<>();
        Set<Map<String, Object>> juris = new HashSet<>();
        Set<String> createdAts = new HashSet<>();

        // Proses data dari detail evaluasi
        for (DetailEvaluasiLapangan detail : details) {
            Team team = teamRepository.findById(detail.getTeamId()).orElse(null);
            Event event = eventRepository.findById(detail.getEventId()).orElse(null);
            UserInfo juri = userRepository.findById(detail.getUserId()).orElse(null);

            if (team != null && event != null && juri != null) {
                Map<String, Object> teamMap = new HashMap<>();
                teamMap.put("id", detail.getTeamId());
                teamMap.put("name", team.getTeamName());
                teams.add(teamMap);  // Tambahkan ke Set untuk menghindari duplikasi

                Map<String, Object> eventMap = new HashMap<>();
                eventMap.put("id", detail.getEventId());
                eventMap.put("name", event.getEventName());
                events.add(eventMap);  // Tambahkan ke Set untuk menghindari duplikasi

                Map<String, Object> juriMap = new HashMap<>();
                juriMap.put("id", detail.getUserId());
                juriMap.put("name", juri.getUsername());
                juris.add(juriMap);  // Tambahkan ke Set untuk menghindari duplikasi

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

    public List<DetailEvaluasiLapanganDTO> getDetailEvaluasi(Long teamId, Long userId, Long eventId, LocalDateTime createdAt) {
        List<Object[]> results = detailEvaluasiLapanganRepository.findDetailEvaluasiByTeamIdAndUserIdAndEventIdAndCreatedAt(teamId, userId, eventId, createdAt);

        if (results.isEmpty()) {
            throw new ResourceNotFoundException("Data tidak ditemukan untuk teamId: " + teamId);
        }

        return results.stream().map(result -> {
            DetailEvaluasiLapanganDTO dto = new DetailEvaluasiLapanganDTO();
            dto.setId((Long) result[0]);
            dto.setPertanyaanId((Long) result[1]);
            dto.setPertanyaan((String) result[2]);
            dto.setScore(Double.valueOf((String) result[3]));
            return dto;
        }).collect(Collectors.toList());
    }


    public void updateScoreLapangan(List<DetailEvaluasiLapangan> evaluations) {
        for (DetailEvaluasiLapangan evaluation : evaluations) {
            DetailEvaluasiLapangan existingEvaluation = detailEvaluasiLapanganRepository.findById(evaluation.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid evaluation ID: " + evaluation.getId()));

            existingEvaluation.setScore(evaluation.getScore().toString());

            detailEvaluasiLapanganRepository.save(existingEvaluation);
        }
    }
}


