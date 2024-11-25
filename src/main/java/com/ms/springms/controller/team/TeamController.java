package com.ms.springms.controller.team;

import com.ms.springms.entity.Team;
import com.ms.springms.model.team.TeamCreateRequest;
import com.ms.springms.model.team.TeamDTO;
import com.ms.springms.model.team.TeamEventWithMemberDTO;
import com.ms.springms.model.utils.PageResponse;
import com.ms.springms.service.team.TeamMemberService;
import com.ms.springms.service.team.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/team")
public class TeamController {

    @Autowired
    private TeamService teamService;

    @Autowired private TeamMemberService teamMemberService;


    @PostMapping("/create")
    public ResponseEntity<TeamDTO> createTeam(@RequestBody TeamCreateRequest teamCreateRequest) {
        TeamDTO createdTeam = teamService.createTeam(teamCreateRequest);
        return new ResponseEntity<>(createdTeam, HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<List<TeamDTO>> getAllTeams() {
        try {
            List<TeamDTO> teams = teamService.getAllTeams();
            return ResponseEntity.ok(teams);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/{teamId}")
    public ResponseEntity<TeamDTO> editTeam(
            @PathVariable Long teamId,
            @RequestBody TeamCreateRequest teamUpdateRequest) {
        TeamDTO updatedTeam = teamService.editTeam(teamId, teamUpdateRequest);
        return ResponseEntity.ok(updatedTeam);
    }


    @GetMapping("/with-member")
    public ResponseEntity<PageResponse<List<TeamEventWithMemberDTO>>> getAllTeamsWithMembersAndEvents(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId,
            @RequestParam( defaultValue = "desc") String sortOrder
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), "createdAt", "teamId");

        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<TeamEventWithMemberDTO> teamPage = teamService.getAllTeamWithEventAndTeamMember(pageable, keyword, userId);
        List<TeamEventWithMemberDTO> teams = teamPage.getContent();
        PageResponse<List<TeamEventWithMemberDTO>> response = new PageResponse<>(
                teams,
                teamPage.getNumber() + 1,
                teamPage.getTotalPages(),
                teamPage.getSize(),
                teamPage.getTotalElements()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamEventWithMemberDTO> getTeamById(@PathVariable Long teamId) {
        TeamEventWithMemberDTO team = teamService.getById(teamId);
        return ResponseEntity.ok(team);
    }
}


