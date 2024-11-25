package com.ms.springms.service.team;

import com.ms.springms.entity.Event;
import com.ms.springms.entity.Team;
import com.ms.springms.model.team.TeamMemberCreateRequest;
import com.ms.springms.model.team.TeamMemberResponse;
import com.ms.springms.repository.team.TeamRepository;
import com.ms.springms.utils.Exceptions.ResourceNotFoundException;
import com.ms.springms.entity.TeamMember;
import com.ms.springms.model.team.TeamMemberDTO;
import com.ms.springms.model.team.UpdateMemberRequest;
import com.ms.springms.repository.event.EventRepository;
import com.ms.springms.repository.team.TeamMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TeamMemberService {


    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private EventRepository eventRepository;


    public TeamMemberResponse createTeamMember(TeamMemberCreateRequest dto) {
        // Validasi input nama anggota
        if (dto.getMemberName() == null || dto.getMemberName().isEmpty()) {
            throw new IllegalArgumentException("Member name cannot be null or empty");
        }

        // Validasi format NIP
        if (dto.getNip() == null || !dto.getNip().matches("\\d{2}-\\d{4}")) {
            throw new IllegalArgumentException("NIP format is invalid. Expected format: XX-XXXX");
        }

        // Cari Team berdasarkan teamId dari DTO
        Team team = teamRepository.findById(dto.getTeamId())
                .orElseThrow(() -> new IllegalArgumentException("Team not found with ID: " + dto.getTeamId()));

        // Cari Event berdasarkan eventId dari DTO
        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + dto.getEventId()));

        // Buat objek TeamMember dan isi dengan data dari DTO
        TeamMember teamMember = new TeamMember();
        teamMember.setTeam(team);
        teamMember.setEvent(event);
        teamMember.setMemberName(dto.getMemberName());
        teamMember.setMemberPosition(dto.getMemberPosition());
        teamMember.setNip(dto.getNip()); // Set nip setelah validasi

        // Simpan anggota tim ke database
        TeamMember savedTeamMember = teamMemberRepository.save(teamMember);

        // Kembalikan hasil sebagai DTO
        return convertMemberToDTO(savedTeamMember);
    }

    // Konversi dari entitas TeamMember ke DTO untuk response
    private TeamMemberResponse convertMemberToDTO(TeamMember teamMember) {
        TeamMemberResponse dto = new TeamMemberResponse();
        dto.setTeamMemberId(teamMember.getTeamMemberId());
        dto.setMemberName(teamMember.getMemberName());
        dto.setMemberPosition(teamMember.getMemberPosition());
        dto.setNip(teamMember.getNip()); // Tambahkan ini
        dto.setTeamId(teamMember.getTeam().getTeamId());
        dto.setEventId(teamMember.getEvent().getEventId());
        return dto;
    }

    private TeamMemberDTO convertToDTO(TeamMember teamMember) {
        TeamMemberDTO dto = new TeamMemberDTO();
        dto.setTeamMemberId(teamMember.getTeamMemberId());
        dto.setMemberName(teamMember.getMemberName());
        dto.setMemberPosition(teamMember.getMemberPosition());
        dto.setNip(teamMember.getNip()); // Tambahkan ini
        return dto;
    }

    public ResponseEntity<?> deleteTeamMember(Long teamMemberId) {
        try {
            TeamMember teamMember = teamMemberRepository.findById(teamMemberId)
                    .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

            teamMemberRepository.delete(teamMember);
            return ResponseEntity.ok("Member with ID: " + teamMemberId + " successfully deleted");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting member: " + ex.getMessage());
        }
    }


    public TeamMemberDTO updateTeamMember(Long teamMemberId, UpdateMemberRequest request) {
        Optional<TeamMember> optionalTeamMember = teamMemberRepository.findById(teamMemberId);
        if (optionalTeamMember.isPresent()) {
            TeamMember existingTeamMember = optionalTeamMember.get();

            // Validasi input
            if (request.getMemberName() == null || request.getMemberName().isEmpty() ||
                    request.getMemberPosition() == null || request.getMemberPosition().isEmpty()) {
                throw new IllegalArgumentException("Member name or Member Position cannot be empty");
            }

            // Update data anggota tim
            existingTeamMember.setMemberName(request.getMemberName());
            existingTeamMember.setMemberPosition(request.getMemberPosition());

            TeamMember result = teamMemberRepository.save(existingTeamMember);

            // Konversi TeamMember menjadi DTO
            return convertToDTO(result);
        } else {
            throw new IllegalArgumentException("Team member not found with ID: " + teamMemberId);
        }
    }


}