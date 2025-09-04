package org.rocman.candidate.services;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
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

        public Candidate uploadCVByEmail (String email, MultipartFile file) throws IOException {
            Candidate candidate = candidateRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Candidate not found"));

            byte[] fileBytes = file.getBytes();
            String extractedText = CVParserUtil.extractText(fileBytes);

            candidate.setCvFile(fileBytes);
            candidate.setCvText(extractedText);

            return candidateRepository.save(candidate);
        }

//    public Optional<Candidate> getCandidateProfile(Long candidateId) {
//        return candidateRepository.findById(candidateId);
//    }
    }


