package org.rocman.candidate.services;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.rocman.candidate.dtos.CandidateRegistrationDTO;
import org.rocman.candidate.entities.Candidate;
import org.rocman.candidate.repositories.CandidateRepository;
import org.rocman.candidate.utils.CVParserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final PasswordEncoder passwordEncoder;

    public Candidate registerCandidate(CandidateRegistrationDTO dto) {
        if (candidateRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        try {
            String fullNumber = dto.getCallingCode() + dto.getPhoneNumber(); // ex: +40712345678
            Phonenumber.PhoneNumber parsed = PhoneNumberUtil.getInstance().parse(fullNumber, null);

            if (!PhoneNumberUtil.getInstance().isValidNumber(parsed)) {
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

            return candidateRepository.save(candidate);
        } catch (NumberParseException e) {
            throw new IllegalArgumentException("Phone number format is invalid");
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


