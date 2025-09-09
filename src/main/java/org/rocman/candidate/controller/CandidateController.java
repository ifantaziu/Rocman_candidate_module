package org.rocman.candidate.controller;

import lombok.RequiredArgsConstructor;
import org.rocman.candidate.entities.Candidate;
import org.rocman.candidate.services.CandidateService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
public class CandidateController {

    private final CandidateService candidateService;

    @PostMapping("/upload-cv")
    public ResponseEntity<?> uploadCV(@RequestParam("file") MultipartFile file) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Received CV upload request from email={}", email);
        try {
            Candidate candidate = candidateService.uploadCVByEmail(email, file);
            log.info("CV upload successful for email={}", email);
            return ResponseEntity.ok(candidate);
        } catch (Exception e) {
            log.error("Error uploading CV for email={}: {}", email, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error uploading CV. Allowed types: PDF, DOC, DOCX, ODT, RTF, TXT. Maximum size: 10 MB." );
        }
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<?> getCandidate(@PathVariable Long id) {
//        return candidateService.getCandidateProfile(id)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
}
