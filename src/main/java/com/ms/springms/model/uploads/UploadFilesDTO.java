package com.ms.springms.model.uploads;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadFilesDTO {

    private Long filesId;
    private String fileName;
    private String filePath;
    private LocalDateTime uploadedAt;
    private String uploadedBy;
    private Long stepId;
    private Long registrationId;
    private Long groupId;
    private String responseFileName;

    private String responseFilePath;

    private LocalDateTime responseUploadedAt;

    private String responseDescription;
}