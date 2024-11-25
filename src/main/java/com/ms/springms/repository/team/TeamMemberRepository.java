package com.ms.springms.repository.team;

import com.ms.springms.entity.Event;
import com.ms.springms.entity.Team;
import com.ms.springms.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamMemberRepository extends JpaRepository<com.ms.springms.entity.TeamMember, Long> {
    List<TeamMember> findByTeamAndEvent(Team team, Event event);

}
