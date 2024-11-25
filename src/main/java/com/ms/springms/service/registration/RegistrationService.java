package com.ms.springms.service.registration;

import com.ms.springms.model.event.EventDTO;
import com.ms.springms.model.registration.*;
import com.ms.springms.model.team.TeamWithMemberDTO;
import com.ms.springms.repository.event.StepRepository;
import com.ms.springms.repository.files.UploadFileGroupRepository;
import com.ms.springms.repository.files.UploadFilesWrapperDTO;
import com.ms.springms.repository.penjurian.TotalPenilaianRepository;
import com.ms.springms.repository.user.UserRepository;
import com.ms.springms.utils.Exceptions.ResourceNotFoundException;
import com.ms.springms.entity.*;
import com.ms.springms.model.event.EventWithStages;
import com.ms.springms.model.uploads.UploadFilesDTO;
import com.ms.springms.model.event.EventStepsDTO;
import com.ms.springms.model.team.TeamMemberDTO;
import com.ms.springms.repository.registration.RegistrationRepository;
import com.ms.springms.repository.files.UploadFileRepository;
import com.ms.springms.repository.event.EventRepository;
import com.ms.springms.repository.team.TeamMemberRepository;
import com.ms.springms.repository.team.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RegistrationService {

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private EventRepository eventRepository;


    @Autowired
    private UploadFileRepository uploadFileRepository;

    @Autowired
    private UploadFileGroupRepository uploadFileGroupRepository;

    @Autowired
    private StepRepository stepRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TotalPenilaianRepository totalPenilaianRepository;
    public void registration(RegistrationRequest registrationRequest) {
        Long teamId = registrationRequest.getTeamId();
        Long eventId = registrationRequest.getEventId();
        String createdBy = registrationRequest.getCreatedBy();
        String judul = registrationRequest.getJudul();

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not Found" + teamId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not Found " + eventId));

        Registration registration = new Registration();
        registration.setTeam(team);
        registration.setEvent(event);
        registration.setCreatedBy(Long.valueOf(createdBy));
        registration.setCreatedAt(LocalDateTime.now());
        registration.setJudul(judul);
        registration.setRegistrationStatus("Pending");
        registrationRepository.save(registration);
    }

    public Page<RegistrationResponseDTO> getAllRegistrations(Pageable pageable) {
        Page<Registration> registrationPage = registrationRepository.findAll(pageable);

        registrationPage.forEach(registration -> {
            // Check if there is any file with the given conditions for the current registration
            boolean hasApprovedRisalahFile = uploadFileRepository.existsByRegistration_RegistrationIdAndApprovalStatusAndIsRisalahTrue(
                    registration.getRegistrationId(),
                    "APPROVE"
            );

            // If such a file exists, update the registration status
            if (hasApprovedRisalahFile) {
                registration.setRegistrationStatus("Completed"); // or any other status you want to set
                registrationRepository.save(registration);
            }
        });

        return registrationPage.map(this::convertToRegistrationResponseDTO);
    }


    public Page<RegistrationResponseDTO> getRegistrationsByCreatedBy(Long createdBy, Pageable pageable) {
        Page<Registration> registrationPage = registrationRepository.findByCreatedBy(createdBy, pageable);
        return registrationPage.map(this::convertToRegistrationResponseDTO);
    }




    public RegistrationResponseDTO getRegistrationById(Long registrationId) {
        try {
            Optional<Registration> registrationOptional = registrationRepository.findById(registrationId);

            if (registrationOptional.isPresent()) {
                Registration registration = registrationOptional.get();

                // Cari semua risalah yang terkait dengan pendaftaran ini
                List<UploadFiles> uploadFilesList = uploadFileRepository.findAllByRegistration_RegistrationIdAndIsRisalahTrue(registration.getRegistrationId());

                // Buat instance baru dari RegistrationResponseDTO
                RegistrationResponseDTO responseDTO = new RegistrationResponseDTO();
                responseDTO.setRegistrationId(registration.getRegistrationId());
                responseDTO.setRegistrationStatus(registration.getRegistrationStatus());
                responseDTO.setJudul(registration.getJudul());

                // Periksa apakah ada file risalah yang ditemukan
                if (!uploadFilesList.isEmpty()) {
                    List<String> fileNames = uploadFilesList.stream().map(UploadFiles::getFileName).collect(Collectors.toList());
                    List<String> filePaths = uploadFilesList.stream().map(UploadFiles::getFilePath).collect(Collectors.toList());
                    responseDTO.setRisalahFileNames(fileNames);
                    responseDTO.setRisalahFilePaths(filePaths);
                } else {
                    // Set nilai default atau biarkan kosong jika tidak ditemukan
                    responseDTO.setRisalahFileNames(Collections.emptyList());
                    responseDTO.setRisalahFilePaths(Collections.emptyList());
                }

                responseDTO.setCreatedBy(String.valueOf(registration.getCreatedBy()));
                responseDTO.setCreatedAt(registration.getCreatedAt());

                // Set RegistrationTeamsDTO
                Team team = registration.getTeam();
                Event eventTeam = registration.getEvent();
                UserInfo userInfo = userRepository.findById(team.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
                List<TeamMember> teamMembers = teamMemberRepository.findByTeamAndEvent(team, eventTeam);
                RegistrationTeamsDTO registrationTeamsDTO = new RegistrationTeamsDTO();
                registrationTeamsDTO.setTeamId(team.getTeamId());
                registrationTeamsDTO.setTeamName(team.getTeamName());
                registrationTeamsDTO.setUserId(String.valueOf(team.getUserId()));
                registrationTeamsDTO.setUsername(userInfo.getUsername());
                registrationTeamsDTO.setTeamMember(convertToTeamMemberDTOList(teamMembers));

                // Set EventWithStages
                Event event = registration.getEvent();
                List<Steps> steps = stepRepository.findByEvent(event);
                EventWithStages eventWithStages = new EventWithStages();
                eventWithStages.setEventId(event.getEventId());
                eventWithStages.setEventName(event.getEventName());
                eventWithStages.setStages(convertToEventStepsDTOList(steps, registration));

                responseDTO.setTeam(registrationTeamsDTO);
                responseDTO.setEvent(eventWithStages);

                return responseDTO;
            } else {
                throw new ResourceNotFoundException("Registrasi dengan ID " + registrationId + " tidak ditemukan");
            }
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Terjadi kesalahan saat mencari registrasi dengan ID " + registrationId, ex);
        }
    }

    public RegistrationResponseDTO updateRegistrationTitle(Long id, String newTitle) {
        return registrationRepository.findById(id)
                .map(existingRegistration -> {
                    existingRegistration.setJudul(newTitle);
                    registrationRepository.save(existingRegistration);
                    return convertToRegistrationUpdate(existingRegistration);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registration not found with id " + id));
    }



    public Page<RegistrationDTO> getAllRegistrationsWithDetailsAndFilesJPQL(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);  // Pagination dimulai dari 1

        // Ambil semua data tanpa filter terlebih dahulu (untuk penetapan rank global)
        List<Object[]> allResults = registrationRepository.findAllRegistrationsWithFilesAndDetailsJPQL(null, Pageable.unpaged()).getContent();

        List<RegistrationDTO> allRegistrations = new ArrayList<>();
        Map<Long, Integer> eventRankMap = new HashMap<>();  // Menyimpan rank global untuk setiap event

        // Proses hasil untuk menetapkan rank global
        for (Object[] result : allResults) {
            Registration registration = (Registration) result[0];
            String resultUsername = (String) result[1];
            Team team = (Team) result[2];
            Event event = (Event) result[3];
            String fileName = (String) result[4];

            // Buat DTO dari hasil query
            RegistrationDTO registrationDTO = new RegistrationDTO();
            registrationDTO.setRegistrationId(registration.getRegistrationId());
            registrationDTO.setJudul(registration.getJudul());
            registrationDTO.setCreatedBy(registration.getCreatedBy());
            registrationDTO.setUsername(resultUsername);
            registrationDTO.setRegistrationStatus(registration.getRegistrationStatus());
            registrationDTO.setCreatedAt(registration.getCreatedAt());

            // Tambahkan data tim yang lebih lengkap
            TeamWithMemberDTO teamDTO = new TeamWithMemberDTO();
            teamDTO.setTeamId(team.getTeamId());
            teamDTO.setTeamName(team.getTeamName());
            teamDTO.setUserId(team.getUserId());  // Ambil userId
            teamDTO.setDescription(team.getDescription());  // Ambil description
            teamDTO.setCreatedAt(team.getCreatedAt());  // Ambil createdAt

            // Jika ada members, tambahkan juga
            List<TeamMemberDTO> memberDTOs = new ArrayList<>();
            if (team.getMembers() != null) {
                for (TeamMember member : team.getMembers()) {
                    TeamMemberDTO memberDTO = new TeamMemberDTO();
                    memberDTO.setTeamMemberId(member.getTeamMemberId());
                    memberDTO.setMemberName(member.getMemberName());
                    memberDTO.setMemberPosition(member.getMemberPosition());
                    memberDTO.setNip(member.getNip());
                    memberDTOs.add(memberDTO);
                }
            }
            teamDTO.setMembers(memberDTOs);
            registrationDTO.setTeam(teamDTO);

            // Tambahkan data event
            EventDTO eventDTO = new EventDTO();
            eventDTO.setEventId(event.getEventId());
            eventDTO.setEventName(event.getEventName());
            eventDTO.setEventYear(event.getEventYear());
            eventDTO.setEventType(event.getEventType());
            registrationDTO.setEvent(eventDTO);

            // Tambahkan file name
            List<String> fileNames = new ArrayList<>();
            if (fileName != null) {
                fileNames.add(fileName);
            }
            registrationDTO.setFileNames(fileNames);

            // Hitung finalScore dan simpan di DTO
            List<Object[]> averageScores = totalPenilaianRepository.findAverageScoresByTeamAndEvent(team.getTeamId(), event.getEventId());
            if (!averageScores.isEmpty()) {
                Object[] scoreData = averageScores.get(0);
                Double avgScoreLapangan = (Double) scoreData[2];
                Double avgScorePresentasi = (Double) scoreData[3];

                // Hitung weighted final score
                double finalScore = (avgScoreLapangan * 0.8) + (avgScorePresentasi * 0.2);
                registrationDTO.setFinalScore(finalScore);
            }

            allRegistrations.add(registrationDTO);
        }

        // Lakukan sorting berdasarkan finalScore
        List<RegistrationDTO> sortedRegistrations = allRegistrations.stream()
                .sorted(Comparator.comparing(RegistrationDTO::getFinalScore).reversed())
                .collect(Collectors.toList());

        // Tetapkan rank global untuk setiap event
        for (RegistrationDTO registration : sortedRegistrations) {
            Long eventId = registration.getEvent().getEventId();
            eventRankMap.putIfAbsent(eventId, 1);  // Mulai rank dari 1

            // Tetapkan rank berdasarkan finalScore dan eventId
            registration.setRank(eventRankMap.get(eventId));

            // Increment rank untuk event yang sedang diproses
            eventRankMap.put(eventId, eventRankMap.get(eventId) + 1);
        }

        // Setelah rank global ditetapkan, lakukan filter sesuai pencarian
        List<RegistrationDTO> filteredRegistrations = sortedRegistrations.stream()
                .filter(reg -> (search == null || reg.getUsername().toLowerCase().contains(search.toLowerCase()) ||
                        reg.getTeam().getTeamName().toLowerCase().contains(search.toLowerCase()) ||
                        reg.getEvent().getEventName().toLowerCase().contains(search.toLowerCase())))
                .collect(Collectors.toList());

        // Paging hasil yang difilter
        int start = Math.min((int) pageable.getOffset(), filteredRegistrations.size());
        int end = Math.min((start + pageable.getPageSize()), filteredRegistrations.size());

        return new PageImpl<>(filteredRegistrations.subList(start, end), pageable, filteredRegistrations.size());
    }

    public List<RegistrationForYelyel> getRegistrationsWithScores(Long juriId) {
        try {
            return registrationRepository.findDistinctRegistrationsWithUsernamesAndScores(juriId);
        } catch (Exception e) {
            // Log error for debugging purposes
            System.err.println("Error fetching registrations with scores: " + e.getMessage());
            throw new RuntimeException("Failed to fetch registrations with scores", e); // Rethrow as a runtime exception
        }
    }

    public List<RegistrationForYelyel> getAllRegistrationsWithoutJuri() {
        try {
            // Memanggil repository tanpa juriId (tanpa filter juri)
            return registrationRepository.findDistinctRegistrationsWithUsernamesAndScoresWithoutJuri();
        } catch (Exception e) {
            // Log error dan lempar ulang sebagai runtime exception
            System.err.println("Error fetching all registrations without juri: " + e.getMessage());
            throw new RuntimeException("Failed to fetch all registrations without juri", e);
        }
    }






//    helper

    private RegistrationResponseDTO convertToRegistrationUpdate(Registration registration) {
        UserInfo user = userRepository.findById(Long.valueOf(registration.getCreatedBy()))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        RegistrationResponseDTO responseDTO = new RegistrationResponseDTO();
        responseDTO.setRegistrationId(registration.getRegistrationId());
        responseDTO.setRegistrationStatus(registration.getRegistrationStatus());
        responseDTO.setCreatedBy(String.valueOf(registration.getCreatedBy()));
        responseDTO.setCreatedAt(registration.getCreatedAt());
        responseDTO.setUsername(user.getUsername());

        return responseDTO;
    }
    private RegistrationResponseDTO convertToRegistrationResponseDTO(Registration registration) {

        UserInfo user = userRepository.findById(Long.valueOf(registration.getCreatedBy())).orElseThrow(() -> new EntityNotFoundException("User not found"));

        RegistrationResponseDTO responseDTO = new RegistrationResponseDTO();
        responseDTO.setRegistrationId(registration.getRegistrationId());
        responseDTO.setRegistrationStatus(registration.getRegistrationStatus());
        responseDTO.setCreatedBy(String.valueOf(registration.getCreatedBy()));
        responseDTO.setCreatedAt(registration.getCreatedAt());
        responseDTO.setUsername((user.getUsername()));
        responseDTO.setJudul(registration.getJudul());

        // Set RegistrationTeamsDTO
        Team team = registration.getTeam();
        Event eventTeam = registration.getEvent();
        List<TeamMember> teamMembers = teamMemberRepository.findByTeamAndEvent(team, eventTeam);
        String username = userRepository.findById(team.getUserId()).get().getUsername();
        RegistrationTeamsDTO registrationTeamsDTO = new RegistrationTeamsDTO();
        registrationTeamsDTO.setTeamId(team.getTeamId());
        registrationTeamsDTO.setTeamName(team.getTeamName());
        registrationTeamsDTO.setUsername(username);
        registrationTeamsDTO.setUserId(String.valueOf(team.getUserId()));
        registrationTeamsDTO.setJudul(registration.getJudul());
        registrationTeamsDTO.setTeamMember(convertToTeamMemberDTOList(teamMembers));

        // Set EventWithStages
        Event event = registration.getEvent();
        List<Steps> steps = stepRepository.findByEvent(event);
        List<EventStepsDTO> eventStepsDTOList = convertToEventStepsDTOList(steps, registration);
        EventWithStages eventWithStages = new EventWithStages();
        eventWithStages.setEventId(event.getEventId());
        eventWithStages.setEventName(event.getEventName());
        eventWithStages.setEventType(event.getEventType());
        eventWithStages.setStages(eventStepsDTOList);

        responseDTO.setTeam(registrationTeamsDTO);
        responseDTO.setEvent(eventWithStages);

        return responseDTO;
    }
    private List<TeamMemberDTO> convertToTeamMemberDTOList(List<TeamMember> teamMembers) {
        List<TeamMemberDTO> teamMemberDTOList = new ArrayList<>();
        for (TeamMember teamMember : teamMembers) {
            TeamMemberDTO teamMemberDTO = new TeamMemberDTO();
            teamMemberDTO.setTeamMemberId(teamMember.getTeamMemberId());
            teamMemberDTO.setMemberName(teamMember.getMemberName());
            teamMemberDTO.setMemberPosition(teamMember.getMemberPosition());
            teamMemberDTO.setNip(teamMember.getNip());
            teamMemberDTOList.add(teamMemberDTO);
        }
        return teamMemberDTOList;
    }

    private List<EventStepsDTO> convertToEventStepsDTOList(List<Steps> steps, Registration registration) {
        List<EventStepsDTO> eventStepsDTOList = new ArrayList<>();
        for (Steps step : steps) {
            EventStepsDTO eventStepsDTO = new EventStepsDTO();
            eventStepsDTO.setStepId(step.getStepId());
            eventStepsDTO.setStepName(step.getStepName());
            eventStepsDTO.setBerkas(step.getBerkas());
            eventStepsDTO.setStartDate(String.valueOf(step.getStartDate()));
            eventStepsDTO.setEndDate(String.valueOf(step.getEndDate()));
            eventStepsDTO.setDescription(step.getDescription());

            // Ambil upload files yang terkait dengan tahap dan registrasi tertentu
            List<UploadFiles> uploadFiles = uploadFileRepository.findByStepsAndRegistration(step, registration);

            // Mengelompokkan upload files berdasarkan groupId
            Map<Long, List<UploadFilesDTO>> groupedFiles = uploadFiles.stream()
                    .collect(Collectors.groupingBy(
                            uploadFile -> uploadFile.getUploadFileGroup().getId(),
                            Collectors.mapping(this::convertToUploadFilesDTO, Collectors.toList())
                    ));

            List<UploadFilesWrapperDTO> uploadFilesWrapperDTOList = new ArrayList<>();

            for (Map.Entry<Long, List<UploadFilesDTO>> entry : groupedFiles.entrySet()) {
                Long groupId = entry.getKey();
                List<UploadFilesDTO> filesDTOList = entry.getValue();

                // Mendapatkan approval status dari UploadFileGroup
                String approvalStatus = uploadFileGroupRepository.findById(groupId)
                        .map(UploadFileGroup::getApprovalStatus)
                        .orElse("UNKNOWN");

                UploadFilesWrapperDTO uploadFilesWrapperDTO = new UploadFilesWrapperDTO();
                uploadFilesWrapperDTO.setApprovalStatus(approvalStatus);
                uploadFilesWrapperDTO.setFiles(filesDTOList);

                uploadFilesWrapperDTOList.add(uploadFilesWrapperDTO);
            }

            eventStepsDTO.setUploadFiles(uploadFilesWrapperDTOList);

            eventStepsDTOList.add(eventStepsDTO);
        }
        return eventStepsDTOList;
    }

    private UploadFilesDTO convertToUploadFilesDTO(UploadFiles file) {
        return new UploadFilesDTO(
                file.getFilesId(),
                file.getFileName(),
                file.getFilePath(),
                file.getUploadedAt(),
                file.getUploadedBy(),
                file.getSteps().getStepId(),
                file.getRegistration().getRegistrationId(),
                file.getUploadFileGroup().getId(),
                file.getResponseFileName(),
                file.getResponseFilePath(),
                file.getResponseUploadedAt(),
                file.getResponseDescription()
        );
    }
}

