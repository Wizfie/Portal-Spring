package com.ms.springms.controller.team;

import com.ms.springms.entity.TeamMember;
import com.ms.springms.model.team.*;
import com.ms.springms.model.utils.Response;
import com.ms.springms.service.team.TeamMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping("/api/member")
public class TeamMemberController {

    @Autowired
    private TeamMemberService teamMemberService;


    @PostMapping("/create")
    public ResponseEntity<TeamMemberResponse> createTeamMember(@RequestBody TeamMemberCreateRequest teamCreateRequest) {
        TeamMemberResponse createdMember = teamMemberService.createTeamMember(teamCreateRequest);
        return new ResponseEntity<>(createdMember, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMember(@PathVariable Long id) {
        try {
            ResponseEntity<?> response = teamMemberService.deleteTeamMember(id);
            return ResponseEntity.ok().body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PutMapping("/{teamMemberId}")
    public ResponseEntity<?> updateTeamMember(@PathVariable Long teamMemberId, @RequestBody UpdateMemberRequest request) {
        try {
            TeamMemberDTO updatedMember = teamMemberService.updateTeamMember(teamMemberId, request);
            return ResponseEntity.ok(updatedMember);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
