package org.rocman.candidate.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.websocket.AuthenticationException;
import org.rocman.candidate.dtos.CandidateRegistrationDTO;
import org.rocman.candidate.entities.Candidate;
import org.rocman.candidate.entities.PasswordResetToken;
import org.rocman.candidate.entities.VerificationToken;
import org.rocman.candidate.repositories.CandidateRepository;
import org.rocman.candidate.repositories.PasswordResetTokenRepository;
import org.rocman.candidate.repositories.VerificationTokenRepository;
import org.rocman.candidate.services.CandidateService;
import org.rocman.candidate.services.EmailService;
import org.rocman.candidate.utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor

public class AuthController {

    private final CandidateService candidateService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final CandidateRepository candidateRepository;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

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

    @GetMapping("/verify")
    public ResponseEntity<String> verifyToken(@RequestParam String token) {
        return verificationTokenRepository.findByToken(token)
                .map(verificationToken -> {
                    if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                        return ResponseEntity.badRequest().body("Expired token");
                    }
                    Candidate candidate = verificationToken.getCandidate();
                    candidate.setEnabled(true);
                    candidateRepository.save(candidate);
                    return ResponseEntity.ok("Account is confirmed!");
                })
                .orElse(ResponseEntity.badRequest().body("Invalid token"));
    }

    @GetMapping("/resend-verification")
    public ResponseEntity<String> resendVerification(@RequestParam String email) {
        Optional<Candidate> candidateOpt = candidateRepository.findByEmail(email);

        if (candidateOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Account not found");
        }

        Candidate candidate = candidateOpt.get();

        if (candidate.isEnabled()) {
            return ResponseEntity.badRequest().body("The account was confirmed");
        }

        Optional<VerificationToken> existingTokenOpt = verificationTokenRepository.findByCandidate(candidate);

        if (existingTokenOpt.isPresent()) {
            VerificationToken existingToken = existingTokenOpt.get();

            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            long recentRequests = verificationTokenRepository
                    .countByCandidateAndCreatedAtAfter(candidate, oneHourAgo);

            if (recentRequests >= 3) {
                return ResponseEntity
                        .status(HttpStatus.TOO_MANY_REQUESTS)
                        .body("You have exceed the limit of 3 emails/ hour. Please try later.");
            }

            if (existingToken.getExpiryDate().isAfter(LocalDateTime.now())) {
                return ResponseEntity
                        .badRequest()
                        .body("Please verify your inbox, there is a valid activation link in previous email.");
            }

            verificationTokenRepository.delete(existingToken);
        }

        String newToken = UUID.randomUUID().toString();
        VerificationToken newVerificationToken = new VerificationToken();
        newVerificationToken.setCandidate(candidate);
        newVerificationToken.setToken(newToken);
        newVerificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        newVerificationToken.setCreatedAt(LocalDateTime.now());
        verificationTokenRepository.save(newVerificationToken);

        emailService.sendVerificationEmail(candidate.getEmail(), newToken);

        return ResponseEntity.ok("The activation link was successfully resented");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        String token = jwtUtil.generateToken(request.getEmail());
        return ResponseEntity.ok(new JwtResponse(token));
    }

    @Data
    static class LoginRequest {
        private String email;
        private String password;
    }

    @Data
    @AllArgsConstructor
    static class JwtResponse {
        private String token;
    }

    @GetMapping("/request-password-reset")
    public ResponseEntity<String> requestPasswordReset(@RequestParam String email) {
        Optional<Candidate> candidateOpt = candidateRepository.findByEmail(email);
        if (candidateOpt.isEmpty()) {
            return ResponseEntity.ok("Please check your inbox for reset password email.");
        }

        Candidate candidate = candidateOpt.get();

        passwordResetTokenRepository.findByCandidate(candidate)
                .ifPresent(passwordResetTokenRepository::delete);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setCandidate(candidate);
        resetToken.setToken(token);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));
        passwordResetTokenRepository.save(resetToken);

        emailService.sendResetPasswordEmail(candidate.getEmail(), token);

        return ResponseEntity.ok("Please check your inbox for reset password email.");
    }

    @GetMapping("/reset-password")
    public ResponseEntity<String> verifyResetToken(@RequestParam String token) {
        return passwordResetTokenRepository.findByToken(token)
                .map(resetToken -> {
                    if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                        return ResponseEntity.badRequest().body("The token has expired.");
                    }
                    return ResponseEntity.ok("Token is valid. You can now reset your password via POST.");
                })
                .orElse(ResponseEntity.badRequest().body("Invalid token."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestBody String newPassword) {
        return passwordResetTokenRepository.findByToken(token)
                .map(resetToken -> {
                    if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                        return ResponseEntity.badRequest().body("The token has expired.");
                    }

                    Candidate candidate = resetToken.getCandidate();
                    candidate.setPassword(passwordEncoder.encode(newPassword));
                    candidate.setEnabled(true);
                    candidateRepository.save(candidate);

                    passwordResetTokenRepository.delete(resetToken);

                    return ResponseEntity.ok("Your password was successfully reset.");
                })
                .orElse(ResponseEntity.badRequest().body("Invalid token."));
    }

//    @PostMapping("/login")
//    public ResponseEntity<Object> login(@RequestBody LoginDTO loginDTO) {
//        Optional<Candidate> candidate = candidateService.authenticate(
//                loginDTO.getEmail(), loginDTO.getPassword()
//        );
//
//        return candidate.<ResponseEntity<Object>>map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(401).body("Invalid credentials"));
//    }
}
