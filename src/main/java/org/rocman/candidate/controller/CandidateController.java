package org.rocman.candidate.controller;

import lombok.RequiredArgsConstructor;
import org.rocman.candidate.entities.Candidate;
import org.rocman.candidate.services.CandidateService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
public class  CandidateController {

    private final CandidateService candidateService;

    @PostMapping("/upload-cv")
    public ResponseEntity<?> uploadCV(@RequestParam("file") MultipartFile file) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            Candidate candidate = candidateService.uploadCVByEmail(email, file);
            return ResponseEntity.ok(candidate);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error uploading CV: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCandidate(@PathVariable Long id) {
        return candidateService.getCandidateProfile(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
