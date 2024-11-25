package com.ms.springms.repository.penjurian.presentasi;


import com.ms.springms.entity.penjurian.presentasi.DetailEvaluasiPresentasi;
import com.ms.springms.model.penjurian.presentasi.DetailEvaluasiPresentasiDTO;
import com.ms.springms.model.penjurian.presentasi.EvaluasiPresentasiDTO;
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
public interface DetailEvaluasiPresentasiRepository extends JpaRepository<DetailEvaluasiPresentasi, Long> {


    List<DetailEvaluasiPresentasi> findByUserIdAndTeamIdAndEventId(Long userId, Long teamId, Long eventId);


    @Query("SELECT d FROM DetailEvaluasiPresentasi d WHERE d.userId = :userId AND d.eventId = :eventId AND FUNCTION('YEAR', d.createdAt) = :year")
    List<DetailEvaluasiPresentasi> findByUserIdAndEventIdAndYear(@Param("userId") Long userId, @Param("eventId") Long eventId, @Param("year") Long year);

    @Query(
            """
                        SELECT 
                            d.userId AS userId, 
                            u.username AS deptName, 
                            d.teamId AS teamId, 
                            t.teamName AS teamName, 
                            d.eventId AS eventId, 
                            e.eventName AS eventName, 
                            d.userId AS juriId, 
                            u2.username AS juriName, 
                            SUM(CAST(d.score AS DOUBLE)) AS score, 
                            d.createdAt AS createdAt
                        FROM DetailEvaluasiPresentasi d
                        JOIN Team t ON d.teamId = t.teamId
                        JOIN UserInfo u ON t.userId = u.id
                        JOIN Event e ON d.eventId = e.eventId
                        JOIN UserInfo u2 ON d.userId = u2.id
                        WHERE 
                            (LOWER(t.teamName) LIKE LOWER(CONCAT('%', :search, '%')) 
                             OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%'))
                             OR LOWER(u2.username) LIKE LOWER(CONCAT('%', :search, '%'))
                             OR LOWER(e.eventName) LIKE LOWER(CONCAT('%', :search, '%')))
                        GROUP BY d.teamId, t.teamName, u.username, d.eventId, e.eventName, d.userId, u2.username, d.createdAt
                        ORDER BY d.createdAt DESC
                    """
    )
    Page<EvaluasiPresentasiDTO> findAllEvaluasiPresentasiBySearchTerm(
            String search,
            Pageable pageable
    );


    @Query("SELECT dep.id, dep.pertanyaanId, pp.pertanyaan, dep.score " +
            "FROM DetailEvaluasiPresentasi dep " +
            "JOIN PertanyaanPresentasi pp ON dep.pertanyaanId = pp.id " +
            "WHERE dep.teamId = :teamId " +
            "AND dep.userId = :userId " +
            "AND dep.eventId = :eventId "
            )
    List<Object[]> findDetailEvaluasiByTeamIdAndUserIdAndEventIdAndCreatedAt(
            @Param("teamId") Long teamId,
            @Param("userId") Long userId,
            @Param("eventId") Long eventId
            );
}

