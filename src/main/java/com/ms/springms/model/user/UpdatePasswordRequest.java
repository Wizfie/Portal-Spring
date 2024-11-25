package com.ms.springms.model.user;

import lombok.Data;

@Data
public class UpdatePasswordRequest {
    private String username;
    private String currentPassword;
    private String newPassword;
}
