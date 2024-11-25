package com.ms.springms.model.event;

import com.ms.springms.model.uploads.UploadFilesDTO;
import com.ms.springms.repository.files.UploadFilesWrapperDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventStepsDTO {
    private Long stepId;
    private String stepName;
    private String startDate;
    private String endDate;
    private String description;
    private String berkas;
    private List<UploadFilesWrapperDTO> uploadFiles;


}
