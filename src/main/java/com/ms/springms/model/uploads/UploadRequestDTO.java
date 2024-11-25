package com.ms.springms.model.uploads;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadRequestDTO {
  private String teamName;
  private String eventName;
  private Long stageId;
  private Long registrationId;

}
