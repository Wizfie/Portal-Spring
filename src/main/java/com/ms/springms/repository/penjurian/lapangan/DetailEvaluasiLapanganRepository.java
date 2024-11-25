package com.ms.springms.repository.penjurian.lapangan;

import com.ms.springms.entity.penjurian.lapangan.DetailEvaluasiLapangan;
import com.ms.springms.model.penjurian.lapangan.EvaluasiLapanganDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DetailEvaluasiLapanganRepository extends JpaRepository<DetailEvaluasiLapangan, Long > {


    @Query("SELECT e FROM DetailEvaluasiLapangan e WHERE e.userId = :userId AND e.teamId = :teamId AND e.eventId = :eventId AND DATE(e.createdAt) = :date")
    List<DetailEvaluasiLapangan> findByUserIdAndTeamIdAndEventIdAndDate(@Param("userId") Long userId, @Param("teamId") Long teamId, @Param("eventId") Long eventId, @Param("date") LocalDate date);


    List<DetailEvaluasiLapangan> findByUserIdAndTeamIdAndEventId(Long userId, Long teamId, Long eventId);

    @Query("SELECT d FROM DetailEvaluasiLapangan d WHERE d.userId = :userId AND d.eventId = :eventId AND FUNCTION('YEAR', d.createdAt) = :year")
    List<DetailEvaluasiLapangan> findByUserIdAndEventIdAndYear(@Param("userId") Long userId, @Param("eventId") Long eventId, @Param("year") Long year);

    @Query("""
                SELECT 
                    d.userId AS userId, 
                    u.username AS deptName, 
                    d.teamId AS teamId, 
                    t.teamName AS teamName, 
                    d.eventId AS eventId, 
                    e.eventName AS eventName, 
                    d.userId AS juriId, 
                    u2.username AS juriName, 
                    SUM(CAST(d.score AS DOUBLE)) AS scoreLapangan, 
                    d.createdAt AS createdAt
                FROM DetailEvaluasiLapangan d
                JOIN Team t ON d.teamId = t.teamId
                JOIN UserInfo u ON t.userId = u.id
                JOIN Event e ON d.eventId = e.eventId
                JOIN UserInfo u2 ON d.userId = u2.id
                WHERE 
                    (LOWER(t.teamName) LIKE LOWER(CONCAT('%', :search, '%')) 
                     OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) 
                     OR LOWER(u2.username) LIKE LOWER(CONCAT('%', :search, '%')) 
                     OR LOWER(e.eventName) LIKE LOWER(CONCAT('%', :search, '%')))
                GROUP BY d.teamId, d.userId, d.eventId, d.createdAt
                ORDER BY d.createdAt DESC
            """)
    Page<EvaluasiLapanganDTO> findAllEvaluasiLapanganBySearchTerm(
            String search,
            Pageable pageable
    );


    @Query("SELECT d.id, d.pertanyaanId, p.pertanyaan, d.score " +
            "FROM DetailEvaluasiLapangan d " +
            "JOIN PertanyaanLapangan p ON d.pertanyaanId = p.id " +
            "WHERE d.teamId = :teamId " +
            "AND d.userId = :userId " +
            "AND d.eventId = :eventId " +
            "AND d.createdAt = :createdAt")
    List<Object[]> findDetailEvaluasiByTeamIdAndUserIdAndEventIdAndCreatedAt(
            @Param("teamId") Long teamId,
            @Param("userId") Long userId,
            @Param("eventId") Long eventId,
            @Param("createdAt") LocalDateTime createdAt);
}