package com.ms.springms.service.team;

import com.ms.springms.entity.Event;
import com.ms.springms.entity.Team;
import com.ms.springms.entity.TeamMember;
import com.ms.springms.entity.UserInfo;
import com.ms.springms.model.event.EventWithTeam;
import com.ms.springms.model.team.TeamCreateRequest;
import com.ms.springms.model.team.TeamDTO;
import com.ms.springms.model.team.TeamEventWithMemberDTO;
import com.ms.springms.model.team.TeamMemberDTO;
import com.ms.springms.repository.team.TeamMemberRepository;
import com.ms.springms.repository.team.TeamRepository;
import com.ms.springms.repository.user.UserRepository;
import com.ms.springms.service.event.EventService;
import io.micrometer.common.util.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UserRepository userRepository;


    public TeamDTO createTeam(TeamCreateRequest teamCreateRequest) {
        Team team = new Team();
        team.setTeamName(teamCreateRequest.getTeamName());
        team.setDescription(teamCreateRequest.getDescription());
        team.setUserId(teamCreateRequest.getUserId());
        team.setCreatedAt(LocalDate.now());  // Set tanggal pembuatan

        // Simpan ke database
        Team savedTeam = teamRepository.save(team);

        return modelMapper.map(savedTeam, TeamDTO.class);
    }
    public TeamDTO editTeam(Long teamId, TeamCreateRequest teamUpdateRequest) {
        // Cari tim berdasarkan ID
        Optional<Team> teamOptional = teamRepository.findById(teamId);

        if (teamOptional.isPresent()) {
            Team team = teamOptional.get();

            // Perbarui informasi tim berdasarkan data baru yang ada di teamUpdateRequest
            team.setTeamName(teamUpdateRequest.getTeamName());
            team.setDescription(teamUpdateRequest.getDescription());

            // Set userId jika diperlukan
            if (teamUpdateRequest.getUserId() != null) {
                team.setUserId(teamUpdateRequest.getUserId());
            }

            // Perbarui createdAt jika ada nilai baru di teamUpdateRequest
            if (teamUpdateRequest.getCreatedAt() != null) {
                team.setCreatedAt(teamUpdateRequest.getCreatedAt());
            }

            // Simpan perubahan ke database
            Team updatedTeam = teamRepository.save(team);

            // Konversi ke DTO dan kembalikan hasil
            return modelMapper.map(updatedTeam, TeamDTO.class);
        } else {
            throw new IllegalArgumentException("Team with ID " + teamId + " not found");
        }
    }


    public Page<TeamEventWithMemberDTO> getAllTeamWithEventAndTeamMember(Pageable pageable, String keyword, Long userId) {
        Page<Team> teams;

        if (StringUtils.isNotBlank(keyword)) {
            teams = teamRepository.findByTeamNameContainingIgnoreCaseOrUserId(keyword, userId, pageable);
        } else if (userId != null) {
            teams = teamRepository.findByUserId(userId, pageable);
        } else {
            teams = teamRepository.findAll(pageable);
        }

        return teams.map(this::convertToDTO);
    }

    public TeamEventWithMemberDTO getById(Long teamId) {
        Optional<Team> teamOptional = teamRepository.findById(teamId);
        if (teamOptional.isPresent()) {
            Team team = teamOptional.get();
            return convertToDTO(team);
        } else {
            throw new IllegalArgumentException("Team with ID " + teamId + " not found");
        }
    }

    public List<TeamDTO> getAllTeams() {
        List<Team> teams = teamRepository.findAll();
        return teams.stream()
                .map(team -> new TeamDTO(
                        team.getTeamId(),
                        team.getTeamName(),
                        team.getDescription(),
                        team.getUserId(),
                        team.getCreatedAt()))
                .collect(Collectors.toList());
    }


    private TeamEventWithMemberDTO convertToDTO(Team team) {
        TeamEventWithMemberDTO teamDTO = modelMapper.map(team, TeamEventWithMemberDTO.class);

        // Ambil username dari userId yang ada di TeamEventWithMemberDTO
        Optional<UserInfo> userInfo = userRepository.findById(team.getUserId());
        userInfo.ifPresent(user -> teamDTO.setUsername(user.getUsername()));  // Set username ke DTO

        List<EventWithTeam> eventDTOs = new ArrayList<>();

        // Kelompokan Team Berdasarkan Event
        Map<Event, List<TeamMember>> memberByEvent = team.getMembers().stream()
                .collect(Collectors.groupingBy(TeamMember::getEvent));

        for (Map.Entry<Event, List<TeamMember>> entry : memberByEvent.entrySet()) {
            Event event = entry.getKey();
            List<TeamMember> members = entry.getValue();

            EventWithTeam eventWithTeam = new EventWithTeam();
            eventWithTeam.setEventId(event.getEventId());
            eventWithTeam.setEventName(event.getEventName());

            List<TeamMemberDTO> memberDTOs = members.stream()
                    .map(member -> {
                        TeamMemberDTO dto = modelMapper.map(member, TeamMemberDTO.class);
                        return dto;
                    })
                    .collect(Collectors.toList());

            eventWithTeam.setMembers(memberDTOs);
            eventDTOs.add(eventWithTeam);
        }
        teamDTO.setTeamEvent(eventDTOs);

        return teamDTO;
    }
}

