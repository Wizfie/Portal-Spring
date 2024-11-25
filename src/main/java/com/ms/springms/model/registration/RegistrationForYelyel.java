package com.ms.springms.model.registration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationForYelyel {
    private Long createdBy;
    private String createdByUsername;
    private Double Score;
    private int year;
    private Long juriId;
    private String juriName;
}
