package org.nbu.medicalrecord.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.nbu.medicalrecord.entities.InviteToken;

@Data
public class AdminInviteRequest {

    @NotNull
    private Long subjectId; // patientId or doctorId

    @NotBlank
    @Email
    private String email;

    @NotNull
    private InviteToken.SubjectType subjectType;
}
