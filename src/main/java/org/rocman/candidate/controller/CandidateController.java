package org.rocman.candidate.controller;

import lombok.RequiredArgsConstructor;
import org.rocman.candidate.dtos.*;
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
    public ResponseEntity<CandidateProfileDTO> uploadCV(@RequestParam("file") MultipartFile file) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Received CV upload request from email={}", email);

        try {
            CandidateProfileDTO dto = candidateService.uploadCVByEmail(email, file);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("Error uploading CV for email={}: {}", email, e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    public record ErrorResponse(String message) {
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<CandidateProfileDTO> getCandidateProfile(@PathVariable Long id) {
        return ResponseEntity.ok(candidateService.getCandidateProfile(id));
    }

    @PutMapping("/edit/profile/{id}")
    public ResponseEntity<CandidateProfileDTO> updateCandidateProfile(
            @PathVariable Long id,
            @RequestBody CandidateProfileDTO dto) {
        log.info("Request PUT update main profile | candidateId={}", id);
        CandidateProfileDTO updatedDto = candidateService.updateCandidateProfile(id, dto);
        log.info("Profile updated successfully | candidateId={}", id);
        return ResponseEntity.ok(updatedDto);
    }

    @PutMapping("/edit/educations/{id}")
    public ResponseEntity<EducationDTO> updateEducation(
            @PathVariable Long id,
            @RequestBody EducationDTO dto) {
        log.info("Request PUT update education | educationId={} | dto={}", id, dto);
        return ResponseEntity.ok(candidateService.updateEducation(id, dto));
    }

    @PutMapping("/edit/experiences/{id}")
    public ResponseEntity<ExperienceDTO> updateExperience(
            @PathVariable Long id,
            @RequestBody ExperienceDTO dto) {
        log.info("Request PUT update experience | experienceId={} | dto={}", id, dto);
        return ResponseEntity.ok(candidateService.updateExperience(id, dto));
    }

    @PutMapping("/edit/skills/{id}")
    public ResponseEntity<SkillDTO> updateSkill(
            @PathVariable Long id,
            @RequestBody SkillDTO dto) {
        log.info("Request PUT update skill | skillId={} | dto={}", id, dto);
        return ResponseEntity.ok(candidateService.updateSkill(id, dto));
    }

    @PutMapping("/edit/languages/{id}")
    public ResponseEntity<LanguageDTO> updateLanguage(
            @PathVariable Long id,
            @RequestBody LanguageDTO dto) {
        log.info("Request PUT update language | languageId={} | dto={}", id, dto);
        return ResponseEntity.ok(candidateService.updateLanguage(id, dto));
    }
}
