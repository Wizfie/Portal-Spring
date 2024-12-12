package com.ms.springms.service.penjurian.yelyel;

import com.ms.springms.entity.UserInfo;
import com.ms.springms.entity.penjurian.presentasi.PointPresentasi;
import com.ms.springms.entity.penjurian.yelyel.DetailEvaluasiYelyel;
import com.ms.springms.entity.penjurian.yelyel.EvaluationYelyelDTO;
import com.ms.springms.entity.penjurian.yelyel.KriteriaYelyel;
import com.ms.springms.entity.penjurian.yelyel.PointPenilaianYelyel;
import com.ms.springms.model.penjurian.yelyel.*;
import com.ms.springms.model.utils.PageResponse;
import com.ms.springms.repository.event.EventRepository;
import com.ms.springms.repository.penjurian.yelyel.DetailEvaluasiYelyelRepository;
import com.ms.springms.repository.penjurian.yelyel.KriteriaYelyelRepository;
import com.ms.springms.repository.penjurian.yelyel.PointPenilaianYelyelRepository;
import com.ms.springms.repository.team.TeamRepository;
import com.ms.springms.repository.user.UserRepository;
import com.ms.springms.service.penjurian.TotalPenilaianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class YelyelQccService {

    @Autowired
    private KriteriaYelyelRepository kriteriaYelyelRepository;

    @Autowired
    private PointPenilaianYelyelRepository pointPenilaianYelyelRepository;

    @Autowired
    private DetailEvaluasiYelyelRepository detailEvaluasiYelyelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TotalPenilaianService totalPenilaianService;

    public KriteriaYelyel savePertanyaanWithPoints(KriteriaYelyel kriteriaYelyel, List<PointPenilaianYelyel> points) {
        KriteriaYelyel savedPertanyaan;

        if (kriteriaYelyel.getId() != null) {
            savedPertanyaan = kriteriaYelyelRepository.findById(kriteriaYelyel.getId()).orElseThrow(() -> new IllegalArgumentException("PertanyaanPresentasi id: "));
        } else {
            kriteriaYelyel.setActive(true);
            savedPertanyaan = kriteriaYelyelRepository.save(kriteriaYelyel);
        }
        for (PointPenilaianYelyel point : points) {
            point.setActive(true);
            point.setKriteriaYelyel(savedPertanyaan);
            pointPenilaianYelyelRepository.save(point);
        }
        return savedPertanyaan;
    }

    public List<PertanyaanYelyelDTO> getPertanyaanYelyel(boolean activeOnly) {
        List<KriteriaYelyel> kriteriaList;

        // Pilih apakah akan mengambil yang active true atau semua
        if (activeOnly) {
            kriteriaList = kriteriaYelyelRepository.findByActiveTrue();
        } else {
            kriteriaList = kriteriaYelyelRepository.findAll();
        }

        return mapKriteriaToPertanyaanDTO(kriteriaList, activeOnly);
    }

    private List<PertanyaanYelyelDTO> mapKriteriaToPertanyaanDTO(List<KriteriaYelyel> kriteriaList, boolean activeOnly) {
        return kriteriaList.stream().map(kriteriaYelyel -> {
            // Pilih point berdasarkan activeOnly
            List<PointYelyelDTO> points;
            if (activeOnly) {
                points = pointPenilaianYelyelRepository.findByKriteriaYelyelAndActiveTrue(kriteriaYelyel)
                        .stream().map(point -> new PointYelyelDTO(
                                point.getId(),
                                point.getPertanyaan(),
                                point.getScoreMaksimal(),
                                point.isActive()
                        )).collect(Collectors.toList());
            } else {
                points = pointPenilaianYelyelRepository.findByKriteriaYelyel(kriteriaYelyel)
                        .stream().map(point -> new PointYelyelDTO(
                                point.getId(),
                                point.getPertanyaan(),
                                point.getScoreMaksimal(),
                                point.isActive()
                        )).collect(Collectors.toList());
            }

            return new PertanyaanYelyelDTO(
                    kriteriaYelyel.getId(),
                    kriteriaYelyel.getName(),
                    kriteriaYelyel.isActive(),
                    points
            );
        }).collect(Collectors.toList());
    }

    public void updateKriteria(Long id, String name, Boolean active) {
        Optional<KriteriaYelyel> optionalKriteriaYelyel = kriteriaYelyelRepository.findById(id);

        if (optionalKriteriaYelyel.isPresent()) {
            KriteriaYelyel kriteriaYelyel = optionalKriteriaYelyel.get();

            // Update pertanyaan hanya jika ada nilai baru
            if (name != null) {
                kriteriaYelyel.setName(name);
            }

            // Update status active hanya jika ada nilai baru
            if (active != null) {
                kriteriaYelyel.setActive(active);

                // Jika status active berubah, update Point yang terkait
                List<PointPenilaianYelyel> points = pointPenilaianYelyelRepository.findByKriteriaYelyel(kriteriaYelyel);
                for (PointPenilaianYelyel point : points) {
                    point.setActive(active);
                    pointPenilaianYelyelRepository.save(point);
                }
            }

            kriteriaYelyelRepository.save(kriteriaYelyel);
        } else {
            throw new RuntimeException("Pertanyaan Presentasi not found");
        }
    }

    public void updatePointPertanyaan(Long id, String pertanyaan, String scoreMaksimal, Boolean active) {
        Optional<PointPenilaianYelyel> optionalPointYelyel = pointPenilaianYelyelRepository.findById(id);

        if (optionalPointYelyel.isPresent()) {
            PointPenilaianYelyel pointPenilaianYelyel = optionalPointYelyel.get();

            // Update pointPenilaian hanya jika ada nilai baru
            if (pertanyaan != null) {
                pointPenilaianYelyel.setPertanyaan(pertanyaan);
            }

            // Update scoreMaksimal hanya jika ada nilai baru
            if (scoreMaksimal != null) {
                pointPenilaianYelyel.setScoreMaksimal(Double.parseDouble(scoreMaksimal));
            }

            // Update status active hanya jika ada nilai baru
            if (active != null) {
                pointPenilaianYelyel.setActive(active);
            }

            pointPenilaianYelyelRepository.save(pointPenilaianYelyel);
        } else {
            throw new RuntimeException("Point Penilaian Presentasi not found");
        }
    }


    public List<DetailEvaluasiYelyel> createEvaluations(List<DetailEvaluasiYelyel> evaluations) {
        double totalScoreYelyel = 0.0;
        Long deptId = null;
        Long juriId = null;
        String deptName = null;

        LocalDateTime createdAt = LocalDateTime.now();

        List<DetailEvaluasiYelyel> evaluationsToSave = new ArrayList<>();

        for (DetailEvaluasiYelyel evaluation : evaluations) {
            evaluation.setCreatedAt(createdAt);
            totalScoreYelyel += Double.parseDouble(evaluation.getScore());

            deptId = evaluation.getDeptId();
            deptName = evaluation.getDeptName();
            juriId = evaluation.getJuriId();

            evaluationsToSave.add(evaluation);
        }

        List<DetailEvaluasiYelyel> savedEvaluations = detailEvaluasiYelyelRepository.saveAll(evaluationsToSave);

        return savedEvaluations;
    }


    // Method untuk mendapatkan semua summary tanpa filter
    public List<ScoreSummary> getAllScoreSummaries() {
        return detailEvaluasiYelyelRepository.findAllScoreSummaries();
    }

    // Method untuk query dinamis berdasarkan parameter opsional
    public List<ScoreSummary> getScoreSummariesByParams(Long juriId, Long deptId, Integer year) {
        return detailEvaluasiYelyelRepository.findScoreSummaryByOptionalParams(juriId, deptId, year);
    }

    public List<DetailEvaluasiYelyelDTO> getEvaluasiByDeptIdAndJuriIdAndYear(Long deptId, Long juriId, int year) {
        try {
            return detailEvaluasiYelyelRepository.findAllByDeptIdAndJuriIdAndYear(deptId, juriId, year);
        } catch (Exception e) {
            // Handle the exception (e.g., log the error)
            System.out.println("Error occurred while fetching data: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve data.");
        }
    }

    public List<DetailEvaluasiYelyel> updateScores(List<UpdateYelyelDTO> scoreUpdates) {
        // List untuk menampung entitas yang akan diupdate
        List<DetailEvaluasiYelyel> detailsToUpdate = new ArrayList<>();

        // Loop untuk memproses setiap perubahan dari DTO
        for (UpdateYelyelDTO entry : scoreUpdates) {
            Long id = entry.getId();
            String newScore = entry.getScore();

            // Cari entitas berdasarkan id
            Optional<DetailEvaluasiYelyel> optionalDetail = detailEvaluasiYelyelRepository.findById(id);
            if (optionalDetail.isPresent()) {
                DetailEvaluasiYelyel detail = optionalDetail.get();
                detail.setScore(newScore);
                // Tambahkan entitas yang sudah diupdate ke list
                detailsToUpdate.add(detail);
            } else {
                throw new RuntimeException("Data with ID " + id + " not found");
            }
        }

        // Simpan semua perubahan dalam satu batch menggunakan saveAll
        return detailEvaluasiYelyelRepository.saveAll(detailsToUpdate);
    }


    public List<HasilEvaluasiYelyelDTO> getEvaluasiYelyelByUser(Long deptId, Long juriId, int year) {
        try {
            List<HasilEvaluasiYelyelDTO> hasil = detailEvaluasiYelyelRepository.findByDeptIdAndJuriIdAndYear(deptId, juriId, year);
            if (hasil.isEmpty()) {
                throw new RuntimeException("Data evaluasi tidak ditemukan untuk kombinasi deptId, juriId, dan tahun yang diberikan.");
            }
            return hasil;
        } catch (Exception e) {
            throw new RuntimeException("Terjadi kesalahan saat mengambil data evaluasi: " + e.getMessage());
        }
    }


    public PageResponse<List<EvaluationYelyelDTO>> evaluationList(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size); // Halaman dimulai dari 1

        // Panggil repository untuk mendapatkan hasil pencarian
        Page<EvaluationYelyelDTO> resultPage = detailEvaluasiYelyelRepository.findBySearchAndYear(search, pageable);

        // Mengembalikan respons dengan paginasi
        return new PageResponse<>(
                resultPage.getContent(),
                resultPage.getNumber() + 1, // Halaman dimulai dari 1
                resultPage.getTotalPages(),
                resultPage.getSize(),
                resultPage.getTotalElements()
        );
    }


    public Map<String, Object> getEvaluasiLapanganAttributes() {
        List<DetailEvaluasiYelyel> details = detailEvaluasiYelyelRepository.findAll();

        // Gunakan Set untuk menghindari duplikasi
        Set<Map<String, Object>> depts = new HashSet<>();
        Set<Map<String, Object>> juris = new HashSet<>();
        Set<String> createdAts = new HashSet<>();

        // Proses data dari detail evaluasi
        for (DetailEvaluasiYelyel detail : details) {
            UserInfo juri = userRepository.findById(detail.getJuriId()).orElse(null);

            if (detail.getDeptName() != null && juri != null) {
                Map<String, Object> deptMap = new HashMap<>();
                deptMap.put("id", detail.getDeptId());
                deptMap.put("name", detail.getDeptName());
                depts.add(deptMap);


                Map<String, Object> juriMap = new HashMap<>();
                juriMap.put("id", detail.getJuriId());
                juriMap.put("name", juri.getUsername());
                juris.add(juriMap);

                createdAts.add(String.valueOf(detail.getCreatedAt().getYear()));
            }
        }
        // Buat map yang berisi list dari masing-masing atribut
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("depts", new ArrayList<>(depts));
        resultMap.put("juris", new ArrayList<>(juris));
        resultMap.put("createdAt", new ArrayList<>(createdAts));

        return resultMap;
    }

    public List<EvaluasiResponse> getEvaluasiByDeptJuriAndCreatedAt(Long deptId, Long juriId, LocalDateTime createdAt) {
        try {
            List<EvaluasiResponse> results = detailEvaluasiYelyelRepository.getEvaluasiByDeptJuriAndCreatedAt(deptId, juriId, createdAt);
            if (results.isEmpty()) {
                throw new Exception("Data not found for the given parameters");
            }
            return results;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch data: " + e.getMessage(), e);
        }
    }

    public void updateScoreYelyel(List<DetailEvaluasiYelyel> evaluations) {
        for (DetailEvaluasiYelyel evaluation : evaluations) {
            DetailEvaluasiYelyel existingEvaluation = detailEvaluasiYelyelRepository.findById(evaluation.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid evaluation ID: " + evaluation.getId()));

            existingEvaluation.setScore(evaluation.getScore().toString());

            detailEvaluasiYelyelRepository.save(existingEvaluation);
        }


    }

    public List<DeptScoreDTO> getAllDeptScoresWithJuriAndYear(Integer year) throws Exception {
        try {
            // Mengambil data dari repository, dengan atau tanpa parameter tahun
            List<Object[]> results = detailEvaluasiYelyelRepository.findAllDeptScoresWithJuriAndYear(year);

            if (results.isEmpty()) {
                throw new Exception("Data tidak ditemukan untuk tahun: " + year);
            }

            // Map untuk mengelompokkan DeptScoreDTO berdasarkan deptId, deptName, dan year
            Map<String, DeptScoreDTO> deptMap = new HashMap<>();

            for (Object[] result : results) {
                Long deptId = (Long) result[0];
                String deptName = (String) result[1];
                Long juriId = (Long) result[2];
                String juriName = (String) result[3];
                Double score = (Double) result[4];
                int resultYear = (int) result[5];

                // Kombinasi deptId, deptName, dan year sebagai key untuk map
                String key = deptId + "-" + deptName + "-" + resultYear;

                DeptScoreDTO deptScoreDTO = deptMap.getOrDefault(key, new DeptScoreDTO(deptId, deptName, resultYear));

                // Menambahkan Juri ke DeptScoreDTO
                deptScoreDTO.addJuri(new JuriScoreDTO(juriId, juriName, score));

                deptMap.put(key, deptScoreDTO);
            }

            // Convert map to list
            List<DeptScoreDTO> deptScoreList = new ArrayList<>(deptMap.values());

            // Sort berdasarkan year DESC dan totalScore DESC
            deptScoreList.sort(Comparator.comparing(DeptScoreDTO::getYear).reversed()
                    .thenComparing(Comparator.comparing(DeptScoreDTO::getTotalScore).reversed()));

            return deptScoreList;

        } catch (Exception e) {
            throw new Exception("Terjadi kesalahan saat mengambil data: " + e.getMessage(), e);
        }
    }
}
