package org.nbu.medicalrecord.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.request.LoginDtoRequest;
import org.nbu.medicalrecord.dtos.response.LoginDtoResponse;
import org.nbu.medicalrecord.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    // login logic

    private final AuthenticationManager authManager;

    private final JwtUtil jwt;

    @PostMapping("/login")
    public ResponseEntity<LoginDtoResponse> login(@Valid @RequestBody LoginDtoRequest req) {
        System.out.println("Success");
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail().trim().toLowerCase(Locale.ROOT), req.getPassword()));


        String token = jwt.generate((UserDetails) auth.getPrincipal());
        return ResponseEntity.ok(new LoginDtoResponse(token, "Bearer", 900L));
    }
}