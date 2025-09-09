package org.rocman.candidate.services;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.apache.tika.Tika;
import org.rocman.candidate.dtos.CandidateRegistrationDTO;
import org.rocman.candidate.entities.Candidate;
import org.rocman.candidate.entities.VerificationToken;
import org.rocman.candidate.repositories.CandidateRepository;
import org.rocman.candidate.repositories.VerificationTokenRepository;
import org.rocman.candidate.utils.CVParserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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

//    public Optional<Candidate> getCandidateProfile(Long candidateId) {
//        return candidateRepository.findById(candidateId);
//    }
}


