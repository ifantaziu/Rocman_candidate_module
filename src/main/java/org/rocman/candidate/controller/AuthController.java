package org.rocman.candidate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.rocman.candidate.dtos.CandidateRegistrationDTO;
import org.rocman.candidate.dtos.LoginDTO;
import org.rocman.candidate.entities.Candidate;
import org.rocman.candidate.services.CandidateService;
import org.rocman.candidate.validation.ValidEmail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CandidateService candidateService;

    @PostMapping("/register")
    public ResponseEntity<Object> register(@Valid @RequestBody CandidateRegistrationDTO dto, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }

        try {
            Candidate candidate = candidateService.registerCandidate(dto);
            return ResponseEntity.ok(candidate);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginDTO loginDTO) {
        Optional<Candidate> candidate = candidateService.authenticate(
                loginDTO.getEmail(), loginDTO.getPassword()
        );

        return candidate.<ResponseEntity<Object>>map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(401).body("Invalid credentials"));
    }
}
