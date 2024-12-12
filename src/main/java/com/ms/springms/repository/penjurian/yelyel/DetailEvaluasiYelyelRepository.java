package com.ms.springms.repository.penjurian.yelyel;

import com.ms.springms.entity.penjurian.presentasi.DetailEvaluasiPresentasi;
import com.ms.springms.entity.penjurian.yelyel.DetailEvaluasiYelyel;
import com.ms.springms.entity.penjurian.yelyel.EvaluationYelyelDTO;
import com.ms.springms.model.penjurian.yelyel.DetailEvaluasiYelyelDTO;
import com.ms.springms.model.penjurian.yelyel.EvaluasiResponse;
import com.ms.springms.model.penjurian.yelyel.HasilEvaluasiYelyelDTO;
import com.ms.springms.model.penjurian.yelyel.ScoreSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DetailEvaluasiYelyelRepository  extends JpaRepository<DetailEvaluasiYelyel , Long> {


    @Query("SELECT new com.ms.springms.model.penjurian.yelyel.ScoreSummary(d.juriId, d.deptId, d.deptName, SUM(CAST(d.score AS double)), MIN(d.createdAt)) " +
            "FROM DetailEvaluasiYelyel d " +
            "GROUP BY d.juriId, d.deptId, d.deptName, YEAR(d.createdAt)")
    List<ScoreSummary> findAllScoreSummaries();

    @Query("SELECT new com.ms.springms.model.penjurian.yelyel.ScoreSummary(d.juriId, d.deptId, d.deptName, SUM(CAST(d.score AS double)), MIN(d.createdAt)) " +
            "FROM DetailEvaluasiYelyel d " +
            "WHERE (:juriId IS NULL OR d.juriId = :juriId) " +
            "AND (:deptId IS NULL OR d.deptId = :deptId) " +
            "AND (:year IS NULL OR YEAR(d.createdAt) = :year) " +
            "GROUP BY d.juriId, d.deptId, d.deptName, YEAR(d.createdAt)")
    List<ScoreSummary> findScoreSummaryByOptionalParams(
            @Param("juriId") Long juriId,
            @Param("deptId") Long deptId,
            @Param("year") Integer year);

    @Query("SELECT new com.ms.springms.model.penjurian.yelyel.DetailEvaluasiYelyelDTO(d.id, d.pertanyaanId, d.score, d.deptId, d.deptName, d.juriId, d.createdAt , py.pertanyaan) " +
            "FROM DetailEvaluasiYelyel d JOIN PointPenilaianYelyel py ON d.pertanyaanId = py.id " +
            "WHERE d.deptId = :deptId AND d.juriId = :juriId AND YEAR(d.createdAt) = :year")
    List<DetailEvaluasiYelyelDTO> findAllByDeptIdAndJuriIdAndYear(Long deptId, Long juriId, int year);


    List<DetailEvaluasiYelyel> findByDeptId(Long deptId);


    @Query("SELECT new com.ms.springms.model.penjurian.yelyel.HasilEvaluasiYelyelDTO(" +
            "d.id, d.deptId, d.deptName, d.juriId, juri.username, p.id, p.pertanyaan, d.score, d.createdAt) " +
            "FROM DetailEvaluasiYelyel d " +
            "JOIN UserInfo juri ON d.juriId = juri.id " +
            "JOIN PointPenilaianYelyel p ON d.pertanyaanId = p.id " +
            "WHERE d.deptId = :deptId AND d.juriId = :juriId AND YEAR(d.createdAt) = :year")
    List<HasilEvaluasiYelyelDTO> findByDeptIdAndJuriIdAndYear(@Param("deptId") Long deptId,
                                                              @Param("juriId") Long juriId,
                                                              @Param("year") int year);


    @Query("SELECT new com.ms.springms.entity.penjurian.yelyel.EvaluationYelyelDTO( " +
            "SUM(CAST(d.score AS double)), d.deptId, d.deptName, d.juriId, u.username, d.createdAt) " +
            "FROM DetailEvaluasiYelyel d " +
            "JOIN UserInfo u ON d.juriId = u.id " +
            "WHERE (:search IS NULL OR d.deptName LIKE %:search% OR u.username LIKE %:search%) " +
            "GROUP BY d.deptId, d.deptName, d.juriId, d.createdAt")
    Page<EvaluationYelyelDTO> findBySearchAndYear(@Param("search") String search,
                                                  Pageable pageable);


    @Query("SELECT new com.ms.springms.model.penjurian.yelyel.EvaluasiResponse(d.id, d.pertanyaanId, p.pertanyaan, CAST(d.score AS double)) " +
            "FROM DetailEvaluasiYelyel d " +
            "JOIN PointPenilaianYelyel p ON d.pertanyaanId = p.id " +
            "WHERE d.deptId = :deptId AND d.juriId = :juriId AND d.createdAt = :createdAt")
    List<EvaluasiResponse> getEvaluasiByDeptJuriAndCreatedAt(@Param("deptId") Long deptId,
                                                             @Param("juriId") Long juriId,
                                                             @Param("createdAt") LocalDateTime createdAt);


    @Query("""
    SELECT d.deptId, 
           d.deptName, 
           d.juriId, 
           u.username, 
           SUM(CAST(d.score AS double)) AS totalScore, 
           YEAR(d.createdAt) AS year
    FROM DetailEvaluasiYelyel d
    JOIN UserInfo u ON d.juriId = u.id
    WHERE (:year IS NULL OR YEAR(d.createdAt) = :year)
    GROUP BY d.deptId, d.deptName, d.juriId, u.username, YEAR(d.createdAt)
""")
    List<Object[]> findAllDeptScoresWithJuriAndYear(Integer year);

}





