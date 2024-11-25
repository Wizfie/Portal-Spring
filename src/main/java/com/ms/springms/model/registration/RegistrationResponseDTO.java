package com.ms.springms.model.registration;

import com.ms.springms.model.event.EventWithStages;
import com.ms.springms.model.uploads.UploadFilesDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationResponseDTO {

    private Long registrationId;
    private String registrationStatus;
    private String judul;
    private String createdBy;
    private String username;
    private List<String> risalahFileNames;
    private List<String> risalahFilePaths;
    private LocalDateTime createdAt;
    private RegistrationTeamsDTO team;
    private EventWithStages event;


}
