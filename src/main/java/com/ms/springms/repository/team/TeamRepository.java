package com.ms.springms.repository.team;

import com.ms.springms.entity.Team;
import com.ms.springms.entity.TeamMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository  extends JpaRepository<Team , Long> {
    Page<Team> findByUserId(Long userId, Pageable pageable);

    Page<Team> findByTeamNameContainingIgnoreCaseOrUserId(String keyword, Long userId, Pageable pageable);

    @Query("SELECT tm FROM TeamMember tm WHERE tm.team.teamId = :teamId AND tm.event.eventId = :eventId")
    List<TeamMember> findTeamMembersByTeamIdAndEventId(@Param("teamId") Long teamId, @Param("eventId") Long eventId);

}


