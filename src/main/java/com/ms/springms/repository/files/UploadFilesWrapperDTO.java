package com.ms.springms.repository.files;

import com.ms.springms.model.uploads.UploadFilesDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadFilesWrapperDTO {
    private String approvalStatus;
    private String isRisalah;
    private List<UploadFilesDTO> files;
}