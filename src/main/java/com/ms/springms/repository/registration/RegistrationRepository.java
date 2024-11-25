package com.ms.springms.repository.registration;

import com.ms.springms.entity.Event;
import com.ms.springms.entity.Registration;
import com.ms.springms.entity.Team;
import com.ms.springms.model.registration.RegistrationForYelyel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration , Long> {

    Page<Registration> findByCreatedBy(Long createdBy, Pageable pageable);
    @Query("SELECT r FROM Registration r WHERE r.team.id = :teamId AND r.event.id = :eventId")


    Registration findByTeamIdAndEventId(@Param("teamId") Long teamId, @Param("eventId") Long eventId);

    @Query("SELECT r, u.username, t, e, uf.fileName FROM Registration r " +
            "JOIN UserInfo u ON r.createdBy = u.id " +
            "JOIN FETCH r.team t " +
            "JOIN r.event e " +
            "LEFT JOIN UploadFiles uf ON uf.registration.registrationId = r.registrationId " +
            "WHERE r.registrationStatus = 'Completed' " +
            "AND uf.approvalStatus = 'APPROVE' AND uf.isRisalah = true " +
            "AND (:search    IS NULL OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(t.teamName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.eventName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY e.eventId")
    Page<Object[]> findAllRegistrationsWithFilesAndDetailsJPQL(
            @Param("search") String searchQuery,
            Pageable pageable);






    @Query("SELECT DISTINCT new com.ms.springms.model.registration.RegistrationForYelyel(r.createdBy, u.username, " +
            "COALESCE((SELECT SUM(CAST(dey.score AS double)) FROM DetailEvaluasiYelyel dey " +
            "WHERE dey.deptId = r.createdBy AND dey.juriId = :juriId AND YEAR(dey.createdAt) = YEAR(CURRENT_DATE)), 0), " +
            "YEAR(r.createdAt), " +  // Mengambil tahun dari createdAt
            "dey.juriId, uj.username) " +  // Mengambil juriId dan nama juri
            "FROM Registration r " +
            "JOIN UserInfo u ON r.createdBy = u.id " +
            "LEFT JOIN DetailEvaluasiYelyel dey ON dey.juriId = :juriId " +  // Gabung dengan DetailEvaluasiYelyel
            "LEFT JOIN UserInfo uj ON dey.juriId = uj.id " +  // Gabung dengan UserInfo untuk mengambil nama juri
            "WHERE YEAR(r.createdAt) = YEAR(CURRENT_DATE)")
    List<RegistrationForYelyel> findDistinctRegistrationsWithUsernamesAndScores(@Param("juriId") Long juriId);


    @Query("SELECT DISTINCT new com.ms.springms.model.registration.RegistrationForYelyel(r.createdBy, u.username, " +
            "COALESCE((SELECT SUM(CAST(dey.score AS double)) FROM DetailEvaluasiYelyel dey " +
            "WHERE dey.deptId = r.createdBy AND dey.juriId = juri.id " +  // Memastikan skor dihitung untuk juriId yang benar
            "AND YEAR(dey.createdAt) = YEAR(CURRENT_DATE)), 0), " +
            "YEAR(r.createdAt), " +  // Mengambil tahun dari createdAt
            "juri.id, uj.username) " +  // Mengambil juriId (sekarang id) dan nama juri dari join ke tabel UserInfo
            "FROM Registration r " +
            "JOIN UserInfo u ON r.createdBy = u.id " +
            "LEFT JOIN DetailEvaluasiYelyel dey ON dey.deptId = r.createdBy " +  // Menghubungkan ke DetailEvaluasiYelyel berdasarkan deptId
            "LEFT JOIN UserInfo uj ON dey.juriId = uj.id " +  // Menghubungkan ke UserInfo untuk mengambil nama juri
            "LEFT JOIN UserInfo juri ON dey.juriId = juri.id " +  // Menambahkan join ke UserInfo untuk mengambil id juri yang benar
            "WHERE YEAR(r.createdAt) = YEAR(CURRENT_DATE) " +
            "AND dey.juriId IS NOT NULL")
    List<RegistrationForYelyel> findDistinctRegistrationsWithUsernamesAndScoresWithoutJuri();





















}
