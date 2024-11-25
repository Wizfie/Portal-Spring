package com.ms.springms.model.registration;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Year;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequest {

    private Long teamId;
    private Long eventId;
    private String createdBy;
    private Year createdAt;
    private String judul;
    private String registrationStatus;
}
