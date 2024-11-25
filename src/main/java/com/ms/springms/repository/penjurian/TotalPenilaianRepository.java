package com.ms.springms.repository.penjurian;

import com.ms.springms.entity.TotalPenilaian;
import com.ms.springms.model.penjurian.totalScore.TeamScoreDTO;
import com.ms.springms.model.penjurian.totalScore.UserScoreDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TotalPenilaianRepository  extends JpaRepository<TotalPenilaian, Long> {
    List<TotalPenilaian> findByTeamIdAndEventIdAndUserId(Long teamId, Long eventId, Long userId);

    @Query("SELECT t FROM TotalPenilaian t " +
            "JOIN Team tm ON t.teamId = tm.id " +
            "JOIN UserInfo u ON t.userId = u.id " +
            "JOIN Event e ON t.eventId = e.id " +
            "WHERE (:searchTerm IS NULL OR :searchTerm = '' OR " +
            "tm.teamName LIKE %:searchTerm% " +
            "OR u.username LIKE %:searchTerm% " +
            "OR e.eventName LIKE %:searchTerm%) " +
            "AND (:startDate IS NULL OR :endDate IS NULL OR t.createdAtLapangan BETWEEN :startDate AND :endDate)")
    Page<TotalPenilaian> searchByTermAndLapanganDate(String searchTerm, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @Query("SELECT t FROM TotalPenilaian t " +
            "JOIN Team tm ON t.teamId = tm.id " +
            "JOIN UserInfo u ON t.userId = u.id " +
            "JOIN Event e ON t.eventId = e.id " +
            "WHERE (:searchTerm IS NULL OR :searchTerm = '' OR " +
            "tm.teamName LIKE %:searchTerm% " +
            "OR u.username LIKE %:searchTerm% " +
            "OR e.eventName LIKE %:searchTerm%) " +
            "AND (:startDate IS NULL OR :endDate IS NULL OR t.createdAtPresentasi BETWEEN :startDate AND :endDate)")
    Page<TotalPenilaian> searchByTermAndPresentasiDate(String searchTerm, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);


    @Query("SELECT new com.ms.springms.model.penjurian.totalScore.UserScoreDTO(" +
            "t.id, " +
            "t.teamId, " +
            "tm.teamName, " +
            "t.eventId, " +
            "e.eventName, " +
            "t.userId, " +
            "u.username, " +
            "t.scoreLapangan, " +
            "t.scorePresentasi) " +  // Corrected the closing parenthesis
            "FROM TotalPenilaian t " +
            "JOIN Team tm ON t.teamId = tm.id " +
            "JOIN Event e ON t.eventId = e.id " +
            "JOIN UserInfo u ON t.userId = u.id " +
            "WHERE t.teamId = :teamId AND t.eventId = :eventId")
    List<UserScoreDTO> findUserScoresByTeamAndEvent(@Param("teamId") Long teamId, @Param("eventId") Long eventId);





    @Query("SELECT new com.ms.springms.model.penjurian.totalScore.TeamScoreDTO(" +
            "u.username AS dept, " +
            "t.teamId, " +
            "tm.teamName, " +
            "t.eventId, " +
            "e.eventName, " +
            "e.eventYear, " +
            "SUM(CAST(t.scoreLapangan AS double)) AS totalScoreLapangan, " +
            "SUM(CAST(t.scorePresentasi AS double)) AS totalScorePresentasi, " +
            "(0.8 * AVG(CAST(t.scoreLapangan AS double)) + 0.2 * AVG(CAST(t.scorePresentasi AS double))) AS nilaiAkhir) " +
            "FROM TotalPenilaian t " +
            "JOIN Team tm ON t.teamId = tm.id " +
            "JOIN Event e ON t.eventId = e.id " +
            "JOIN UserInfo u ON tm.userId = u.id " +
            "GROUP BY u.username, t.teamId, tm.teamName, t.eventId, e.eventName, e.eventYear")
    List<TeamScoreDTO> findAllTeamScores();




    List<TotalPenilaian> findByTeamIdAndEventId(Long teamId, Long eventId);

    @Query("SELECT tp.teamId, tp.eventId, " +
            "AVG(CAST(tp.scoreLapangan AS double)), " +
            "AVG(CAST(tp.scorePresentasi AS double)) " +
            "FROM TotalPenilaian tp " +
            "WHERE tp.teamId = :teamId AND tp.eventId = :eventId " +
            "GROUP BY tp.teamId, tp.eventId")
    List<Object[]> findAverageScoresByTeamAndEvent(@Param("teamId") Long teamId, @Param("eventId") Long eventId);




}