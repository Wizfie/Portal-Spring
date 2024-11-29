package com.ms.springms.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminEmailDTO {

    private String email;
    private String emailPassword;
}
