package com.ms.springms.service.penjurian;

import com.ms.springms.entity.*;
import com.ms.springms.model.penjurian.TotalEvaluasiDTO;
import com.ms.springms.model.penjurian.UpdateScoreRequest;
import com.ms.springms.model.penjurian.totalScore.TeamScoreDTO;
import com.ms.springms.model.penjurian.totalScore.UserScoreDTO;
import com.ms.springms.model.team.TeamMemberDTO;
import com.ms.springms.model.utils.PageResponse;
import com.ms.springms.repository.event.EventRepository;
import com.ms.springms.repository.registration.RegistrationRepository;
import com.ms.springms.repository.files.UploadFileRepository;
import com.ms.springms.repository.penjurian.TotalPenilaianRepository;
import com.ms.springms.repository.team.TeamRepository;
import com.ms.springms.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TotalPenilaianService {

    @Autowired
    private TotalPenilaianRepository totalPenilaianRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private UploadFileRepository uploadFileRepository;



    @Transactional
    public void updateScore(Long teamId, Long eventId, Long userId, double updatedScore, String scoreType) {

        List<TotalPenilaian> penilaianList = totalPenilaianRepository.findByTeamIdAndEventIdAndUserId(teamId, eventId, userId);
        LocalDateTime now = LocalDateTime.now();

        if (penilaianList.isEmpty()) {
            // Membuat entitas baru jika tidak ditemukan
            TotalPenilaian newPenilaian = new TotalPenilaian();
            newPenilaian.setTeamId(teamId);
            newPenilaian.setEventId(eventId);
            newPenilaian.setUserId(userId);
            setScore(newPenilaian, updatedScore, scoreType, now);
            totalPenilaianRepository.save(newPenilaian);
        } else {
            // Mengupdate entitas yang ditemukan
            for (TotalPenilaian penilaian : penilaianList) {
                setScore(penilaian, updatedScore, scoreType, null);
                totalPenilaianRepository.save(penilaian);
            }
        }
    }

    public PageResponse<List<TotalEvaluasiDTO>> getScoreTotal(
            int page,
            int size,
            String search,
            LocalDate startDate,
            LocalDate endDate,
            String type) {

        PageRequest pageRequest = PageRequest.of(page - 1, size);
        Page<TotalPenilaian> penilaianPage;

        boolean isSearchTermEmpty = (search == null || search.isEmpty());
        boolean isDateRangeValid = (startDate != null && endDate != null);

        // Logging untuk memastikan nilai parameter
        System.out.println("Search Term: " + search);
        System.out.println("Start Date: " + startDate);
        System.out.println("End Date: " + endDate);
        System.out.println("Type: " + type);

        if (isSearchTermEmpty && !isDateRangeValid) {
            penilaianPage = totalPenilaianRepository.findAll(pageRequest);
        } else {
            penilaianPage = switch (type) {
                case "Lapangan" -> totalPenilaianRepository.searchByTermAndLapanganDate(
                        search,
                        isDateRangeValid ? startDate.atStartOfDay() : null,
                        isDateRangeValid ? endDate.atTime(23, 59, 59) : null,
                        pageRequest
                );
                case "Presentasi" -> totalPenilaianRepository.searchByTermAndPresentasiDate(
                        search,
                        isDateRangeValid ? startDate.atStartOfDay() : null,
                        isDateRangeValid ? endDate.atTime(23, 59, 59) : null,
                        pageRequest
                );

                default -> throw new IllegalArgumentException("Invalid type: " + type);
            };
        }

        List<TotalEvaluasiDTO> dtos = penilaianPage.getContent()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageResponse<>(
                dtos,
                penilaianPage.getNumber() + 1,
                penilaianPage.getTotalPages(),
                penilaianPage.getSize(),
                penilaianPage.getTotalElements()
        );
    }

    public List<TeamScoreDTO> getFinalScore() {
        List<TeamScoreDTO> teamScores = totalPenilaianRepository.findAllTeamScores();

        for (TeamScoreDTO teamScore : teamScores) {
            // Retrieve UserScores based on teamId and eventId
            List<UserScoreDTO> userScores = totalPenilaianRepository.findUserScoresByTeamAndEvent(teamScore.getTeamId(), teamScore.getEventId());
            teamScore.setUserScores(userScores);

            // Fetch Registration data based on teamId and eventId
            Registration registration = registrationRepository.findByTeamIdAndEventId(teamScore.getTeamId(), teamScore.getEventId());

            if (registration != null) {
                teamScore.setJudul(registration.getJudul());

                // Fetch all risalah file details from UploadFiles using the repository
                List<UploadFiles> uploadFilesList = uploadFileRepository.findAllByRegistration_RegistrationIdAndApprovalStatusAndIsRisalah(
                        registration.getRegistrationId(), "APPROVE", true);

                for (UploadFiles uploadFiles : uploadFilesList) {
                    teamScore.getRisalahFileNames().add(uploadFiles.getFileName());
                    teamScore.getRisalahFilePaths().add(uploadFiles.getFilePath());
                }

                // Handle case when no risalah file is found
                if (uploadFilesList.isEmpty()) {
                    teamScore.setRisalahFileNames(Collections.emptyList());
                    teamScore.setRisalahFilePaths(Collections.emptyList());
                }
            }

            // Retrieve TeamMember data based on teamId and eventId
            List<TeamMember> teamMembers = teamRepository.findTeamMembersByTeamIdAndEventId(teamScore.getTeamId(), teamScore.getEventId());
            List<TeamMemberDTO> teamMemberDTOs = teamMembers.stream()
                    .map(member -> new TeamMemberDTO(member.getTeamMemberId(), member.getMemberName(), member.getMemberPosition() ,member.getNip() ))
                    .collect(Collectors.toList());
            teamScore.setTeamMembers(teamMemberDTOs);
        }

        return teamScores;
    }



    @Transactional
    public Optional<TotalPenilaian> updateScoreFinal(Long id, UpdateScoreRequest updateScoreRequest) {
        Optional<TotalPenilaian> optionalTotalPenilaian = totalPenilaianRepository.findById(id);
        if (optionalTotalPenilaian.isPresent()) {
            TotalPenilaian totalPenilaian = optionalTotalPenilaian.get();
            switch (updateScoreRequest.getScoreType().toLowerCase()) {
                case "scorelapangan":
                    totalPenilaian.setScoreLapangan(updateScoreRequest.getScoreValue());
                    break;
                case "scorepresentasi":
                    totalPenilaian.setScorePresentasi(updateScoreRequest.getScoreValue());
                    break;
//                case "scoreyelyel":
//                    totalPenilaian.setScoreYelyel(updateScoreRequest.getScoreValue());
//                    break;
                default:
                    throw new IllegalArgumentException("Invalid score type");
            }
            totalPenilaianRepository.save(totalPenilaian);
            return Optional.of(totalPenilaian);
        }
        return Optional.empty();
    }




//    Helper
    private TotalEvaluasiDTO convertToDTO(TotalPenilaian penilaian) {
        Team team = teamRepository.findById(penilaian.getTeamId()).orElse(null);
        UserInfo user = userRepository.findById(team.getUserId()).orElse(null);

        TotalEvaluasiDTO dto = new TotalEvaluasiDTO();
        dto.setId(penilaian.getId());
        dto.setTeamId(penilaian.getTeamId());
        dto.setEventId(penilaian.getEventId());
        dto.setUserId(penilaian.getUserId());
        dto.setDept(user.getUsername());
        dto.setScoreLapangan(penilaian.getScoreLapangan());
        dto.setScorePresentasi(penilaian.getScorePresentasi());
//        dto.setScoreYelyel(penilaian.getScoreYelyel());
        dto.setCreatedAtLapangan(penilaian.getCreatedAtLapangan());
        dto.setCreatedAtPresentasi(penilaian.getCreatedAtPresentasi());
//        dto.setCreatedAtYelyel(penilaian.getCreatedAtYelyel());

        dto.setTeamName(getTeamNameById(penilaian.getTeamId()));
        dto.setEventName(getEventNameById(penilaian.getEventId()));
        dto.setUsername(getUsernameById(penilaian.getUserId()));

        return dto;
    }

private void setScore(TotalPenilaian penilaian, double score, String scoreType, LocalDateTime dateTime) {
    switch (scoreType.toLowerCase()) {
        case "lapangan":
            penilaian.setScoreLapangan(String.valueOf(score));
            if (dateTime != null) {
                penilaian.setCreatedAtLapangan(dateTime);
            }
            break;
        case "presentasi":
            penilaian.setScorePresentasi(String.valueOf(score));
            if (dateTime != null) {
                penilaian.setCreatedAtPresentasi(dateTime);
            }
            break;
//        case "yelyel":
//            penilaian.setScoreYelyel(String.valueOf(score));
//            if (dateTime != null) {
//                penilaian.setCreatedAtYelyel(dateTime);
//            }
//            break;
    }
}

    private String getTeamNameById(Long teamId) {
        Team team = teamRepository.findById(teamId).orElse(null);
        return (team != null) ? team.getTeamName() : null;
    }

    private String getUsernameById(Long userId) {
        UserInfo user = userRepository.findById(userId).orElse(null);
        return (user != null) ? user.getUsername() : null;
    }

    private String getEventNameById(Long eventId) {
        Event event = eventRepository.findById(eventId).orElse(null);
        return (event != null) ? event.getEventName() : null;
    }

}
