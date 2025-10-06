package org.nbu.medicalrecord.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AcceptInviteRequest {

    @NotBlank
    private String token;

    @NotBlank
    @Size(min=8, max=72)
    private String password;

}
