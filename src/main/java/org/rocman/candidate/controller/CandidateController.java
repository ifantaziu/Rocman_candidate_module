package org.rocman.candidate.controller;

import lombok.RequiredArgsConstructor;
import org.rocman.candidate.dtos.CandidateProfileDTO;
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
            log.info("CV upload successful for email={}", email);
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
        return ResponseEntity.ok(updatedDto);
    }

    @PutMapping("/edit/educations/{id}")
    public ResponseEntity<CandidateProfileDTO.EducationDTO> updateEducation(
            @PathVariable Long id,
            @RequestBody CandidateProfileDTO.EducationDTO dto) {
        return ResponseEntity.ok(candidateService.updateEducation(id, dto));
    }

    @PutMapping("/edit/experiences/{id}")
    public ResponseEntity<CandidateProfileDTO.ExperienceDTO> updateExperience(
            @PathVariable Long id,
            @RequestBody CandidateProfileDTO.ExperienceDTO dto) {
        return ResponseEntity.ok(candidateService.updateExperience(id, dto));
    }

    @PutMapping("/edit/skills/{id}")
    public ResponseEntity<CandidateProfileDTO.SkillDTO> updateSkill(
            @PathVariable Long id,
            @RequestBody CandidateProfileDTO.SkillDTO dto) {
        return ResponseEntity.ok(candidateService.updateSkill(id, dto));
    }

    @PutMapping("/edit/languages/{id}")
    public ResponseEntity<CandidateProfileDTO.LanguageDTO> updateLanguage(
            @PathVariable Long id,
            @RequestBody CandidateProfileDTO.LanguageDTO dto) {
        return ResponseEntity.ok(candidateService.updateLanguage(id, dto));
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<?> getCandidate(@PathVariable Long id) {
//        return candidateService.getCandidateProfile(id)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
}
