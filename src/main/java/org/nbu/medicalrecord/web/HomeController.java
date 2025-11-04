package org.nbu.medicalrecord.web;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {

    @GetMapping("/home")
    public ResponseEntity<Void> home() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/home")
    public ResponseEntity<Void> homeAdmin() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/doctor/home")
    public ResponseEntity<Void> homeDoctor() {
        return ResponseEntity.ok().build();
    }
}
