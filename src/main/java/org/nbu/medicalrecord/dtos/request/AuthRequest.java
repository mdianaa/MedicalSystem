package org.nbu.medicalrecord.dtos.request;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
}