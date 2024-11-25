package com.ms.springms.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StepsDTO {
    private Long stepId;
    private String stepName;
    private Date startDate;
    private Date endDate;
    private String description;
    private String berkas;
}
