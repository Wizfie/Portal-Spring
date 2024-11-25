package com.ms.springms.model.user;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String username;
    private String newPassword;
}