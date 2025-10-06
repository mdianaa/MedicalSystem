package org.nbu.medicalrecord.dtos.response;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoginDtoResponse {

    @NonNull
    private String accessToken;

    @NonNull
    private String tokenType;

    @NotNull
    private Long expiresInSeconds;
}
