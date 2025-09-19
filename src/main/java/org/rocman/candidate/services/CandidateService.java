package org.rocman.candidate.services;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import jakarta.persistence.EntityNotFoundException;
import org.apache.tika.Tika;
import org.rocman.candidate.dtos.CandidateProfileDTO;
import org.rocman.candidate.dtos.CandidateRegistrationDTO;
import org.rocman.candidate.entities.*;
import org.rocman.candidate.repositories.*;
import org.rocman.candidate.utils.CVDataExtractor;
import org.rocman.candidate.utils.CVParserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final Tika tika = new Tika();
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "application/msword", // .doc
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
            "application/vnd.oasis.opendocument.text", // .odt
            "application/rtf",
            "text/plain"
    );
    private final EducationRepository educationRepository;
    private final ExperienceRepository experienceRepository;
    private final SkillRepository skillRepository;
    private final LanguageRepository languageRepository;

    public Candidate registerCandidate(CandidateRegistrationDTO dto) {
        if (candidateRepository.findByEmail(dto.getEmail()).isPresent()) {
            log.warn("Registration validation failed | reason=Email already registered | email={} | timestamp={}",
                    dto.getEmail(), LocalDateTime.now());
            throw new IllegalArgumentException("Email already registered");
        }

        try {
            String fullNumber = dto.getCallingCode() + dto.getPhoneNumber();
            Phonenumber.PhoneNumber parsed = PhoneNumberUtil.getInstance().parse(fullNumber, null);

            if (!PhoneNumberUtil.getInstance().isValidNumber(parsed)) {
                log.warn("Registration validation failed | reason=Invalid phone number | email={} | timestamp={}",
                        dto.getEmail(), LocalDateTime.now());
                throw new IllegalArgumentException("Invalid phone number");
            }

            String e164Formatted = PhoneNumberUtil.getInstance()
                    .format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164);

            Candidate candidate = new Candidate();
            candidate.setEmail(dto.getEmail());
            candidate.setPassword(passwordEncoder.encode(dto.getPassword()));
            candidate.setFirstName(dto.getFirstName());
            candidate.setLastName(dto.getLastName());
            candidate.setPhoneNumber(e164Formatted);
            candidate.setEnabled(false);

            candidateRepository.save(candidate);

            log.debug("DB operations | action= Register user | entity=Candidate | userId={} | email={} | timestamp={}",
                    candidate.getId(), candidate.getEmail(), LocalDateTime.now());

            String token = UUID.randomUUID().toString();
            VerificationToken verificationToken = new VerificationToken();
            verificationToken.setCandidate(candidate);
            verificationToken.setToken(token);
            verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
            verificationTokenRepository.save(verificationToken);

            emailService.sendVerificationEmail(candidate.getEmail(), token);
            log.info("New user registered | userId={} | email={} | timestamp={}",
                    candidate.getId(), candidate.getEmail(), LocalDateTime.now());

            return candidate;

        } catch (NumberParseException e) {
            log.warn("Registration validation failed | reason=Phone number format is invalid | email={} | timestamp={}",
                    dto.getEmail(), LocalDateTime.now(), e);
            throw new IllegalArgumentException("Phone number format is invalid");
        } catch (Exception e) {
            log.error("Unexpected error in registerCandidate | email={} | timestamp={}",
                    dto.getEmail(), LocalDateTime.now(), e);
            throw new RuntimeException("Internal server error");
        }
    }


//    public Optional<Candidate> authenticate(String email, String password) {
//        return candidateRepository.findByEmail(email)
//                .filter(candidate -> passwordEncoder.matches(password, candidate.getPassword()));
//    }

    public Candidate uploadCVByEmail(String email, MultipartFile file) throws IOException {
        log.info("Starting CV upload for candidate with email={}", email);
        Candidate candidate = candidateRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Candidate not found for email={}", email);
                    return new RuntimeException("Candidate not found");
                });

        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("File too large (Maximum allowed size is 10 MB): {} bytes (email={})", file.getSize(), email);
            throw new RuntimeException("File too large. Maximum allowed size is 10 MB.");
        }

        String contentType = file.getContentType();
        String detectedType = tika.detect(file.getInputStream());

        log.info("File contentType from request: {}", contentType);
        log.info("File type detected by Tika: {}", detectedType);

        if (!isAllowedType(contentType) && !isAllowedType(detectedType)) {
            log.warn("Unsupported file type: requestType={}, detectedType={} (email={})",
                    contentType, detectedType, email);
            throw new RuntimeException("Unsupported file type. Allowed types are: PDF, DOC, DOCX, ODT, RTF, TXT. " + detectedType +
                    "Maximum size: 10 MB.");
        }

        log.info("File validation passed for email={}, type={}, size={} bytes",
                email, detectedType, file.getSize());

        String extractedText;
        try {
            extractedText = CVParserUtil.extractText(file.getInputStream());
            log.info("Successfully extracted text from CV for email={}", email);
        } catch (Exception e) {
            log.error("Failed to parse CV for email={}. Error: {}", email, e.getMessage(), e);
            throw new RuntimeException("Could not process the uploaded CV. Please ensure it is a valid PDF, DOCX, or similar document.");
        }

        candidate.setCvFile(file.getBytes());
        candidate.setCvText(extractedText);

        Candidate savedCandidate = candidateRepository.save(candidate);
        log.info("CV upload completed successfully for email={}", email);

        return savedCandidate;
    }

    private boolean isAllowedType(String mimeType) {
        return mimeType != null && ALLOWED_MIME_TYPES.contains(mimeType);
    }

    @Transactional(readOnly = true)
    public CandidateProfileDTO getCandidateProfile(Long id) {
        log.info("Loading candidate profile | candidateId={}", id);

        Candidate candidate = candidateRepository.findByIdWithAllRelations(id)
                .orElseThrow(() -> {
                    log.warn("Candidate not found | candidateId={}", id);
                    return new EntityNotFoundException("Candidate not found");
                });

        CandidateProfileDTO dto = new CandidateProfileDTO();
        dto.setId(candidate.getId());
        dto.setEmail(candidate.getEmail());
        dto.setPhone(candidate.getPhoneNumber());
        dto.setFirstName(candidate.getFirstName());
        dto.setLastName(candidate.getLastName());
        String address = CVDataExtractor.extractAddress(candidate.getCvText());
        dto.setAddress(address != null ? address : "");

        dto.setEducation(candidate.getEducations().stream().map(e -> {
            CandidateProfileDTO.EducationDTO edto = new CandidateProfileDTO.EducationDTO();
            edto.setId(e.getId());
            edto.setLevel(e.getLevel());
            edto.setInstitution(e.getInstitution());
            edto.setPeriod(e.getPeriod());
            return edto;
        }).toList());

        dto.setExperience(candidate.getExperiences().stream().map(e -> {
            CandidateProfileDTO.ExperienceDTO exdto = new CandidateProfileDTO.ExperienceDTO();
            exdto.setId(e.getId());
            exdto.setTitle(e.getTitle());
            exdto.setCompany(e.getCompany());
            exdto.setPeriod(e.getPeriod());
            return exdto;
        }).toList());

        dto.setSkills(candidate.getSkills().stream().map(s -> {
            CandidateProfileDTO.SkillDTO sdto = new CandidateProfileDTO.SkillDTO();
            sdto.setId(s.getId());
            sdto.setName(s.getName());
            return sdto;
        }).toList());

        dto.setLanguages(candidate.getLanguages().stream().map(l -> {
            CandidateProfileDTO.LanguageDTO ldto = new CandidateProfileDTO.LanguageDTO();
            ldto.setId(l.getId());
            ldto.setLanguage(l.getLanguage());
            ldto.setLevel(l.getLevel());
            return ldto;
        }).toList());

        log.info("Profile fetched successfully | candidateId={}", id);
        return dto;
    }

    @Transactional
    public CandidateProfileDTO updateCandidateProfile(Long id, CandidateProfileDTO dto) {
        log.info("Updating candidate main profile | candidateId={}", id);

        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Candidate not found | candidateId={}", id);
                    return new EntityNotFoundException("Candidate not found");
                });

        if (dto.getEmail() != null) candidate.setEmail(dto.getEmail());
        if (dto.getPhone() != null) candidate.setPhoneNumber(dto.getPhone());
        if (dto.getLastName() != null) candidate.setLastName(dto.getLastName());
        if (dto.getAddress() != null) candidate.setCvText(updateCvTextWithAddress(candidate.getCvText(), dto.getAddress()));

        candidateRepository.save(candidate);
        log.info("Candidate main profile updated successfully | candidateId={}", id);

        return getCandidateProfile(id);
    }

    private String updateCvTextWithAddress(String cvText, String newAddress) {
        if (cvText == null) return newAddress;
        return cvText + "\nAddress: " + newAddress;
    }

    @Transactional
    public CandidateProfileDTO.EducationDTO updateEducation(Long id, CandidateProfileDTO.EducationDTO dto) {
        log.info("Updating Education | educationId={}", id);

        Education education = educationRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Education not found | educationId={}", id);
                    return new EntityNotFoundException("Education not found");
                });

        education.setLevel(dto.getLevel());
        education.setInstitution(dto.getInstitution());
        education.setPeriod(dto.getPeriod());
        educationRepository.save(education);

        log.info("Education updated successfully | educationId={}", id);
        dto.setId(education.getId());
        return dto;
    }

    @Transactional
    public CandidateProfileDTO.ExperienceDTO updateExperience(Long id, CandidateProfileDTO.ExperienceDTO dto) {
        log.info("Updating Experience | experienceId={}", id);

        Experience experience = experienceRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Experience not found | experienceId={}", id);
                    return new EntityNotFoundException("Experience not found");
                });

        experience.setTitle(dto.getTitle());
        experience.setCompany(dto.getCompany());
        experience.setPeriod(dto.getPeriod());
        experienceRepository.save(experience);

        log.info("Experience updated successfully | experienceId={}", id);
        dto.setId(experience.getId());
        return dto;
    }

    @Transactional
    public CandidateProfileDTO.SkillDTO updateSkill(Long id, CandidateProfileDTO.SkillDTO dto) {
        log.info("Updating Skill | skillId={}", id);

        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Skill not found | skillId={}", id);
                    return new EntityNotFoundException("Skill not found");
                });

        skill.setName(dto.getName());
        skillRepository.save(skill);

        log.info("Skill updated successfully | skillId={}", id);
        dto.setId(skill.getId());
        return dto;
    }

    @Transactional
    public CandidateProfileDTO.LanguageDTO updateLanguage(Long id, CandidateProfileDTO.LanguageDTO dto) {
        log.info("Updating Language | languageId={}", id);

        Language language = languageRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Language not found | languageId={}", id);
                    return new EntityNotFoundException("Language not found");
                });

        language.setLanguage(dto.getLanguage());
        language.setLevel(dto.getLevel());
        languageRepository.save(language);

        log.info("Language updated successfully | languageId={}", id);
        dto.setId(language.getId());
        return dto;
    }

//    public Optional<Candidate> getCandidateProfile(Long candidateId) {
//        return candidateRepository.findById(candidateId);
//    }
}


