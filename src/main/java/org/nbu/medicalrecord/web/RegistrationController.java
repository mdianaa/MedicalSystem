package org.nbu.medicalrecord.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.request.AcceptInviteRequest;
import org.nbu.medicalrecord.services.InvitationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class RegistrationController {

    // TODO: currently not in use
    // registration logic for the user

    private final InvitationService invitationService;

    @PostMapping("/accept-invite")
    public ResponseEntity<Void> accept(@Valid @RequestBody AcceptInviteRequest req) {
        invitationService.acceptInvite(req);
        return ResponseEntity.noContent().build();
    }
}
