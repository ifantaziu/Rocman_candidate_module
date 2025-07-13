package org.rocman.candidate.services;

import org.rocman.candidate.dtos.CandidateRegistrationDTO;
import org.rocman.candidate.entities.Candidate;
import org.rocman.candidate.repositories.CandidateRepository;
import org.rocman.candidate.utils.CVParserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final PasswordEncoder passwordEncoder;

    public Candidate registerCandidate(CandidateRegistrationDTO dto) {
        if (candidateRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        Candidate candidate = new Candidate();
        candidate.setEmail(dto.getEmail());
        candidate.setPassword(passwordEncoder.encode(dto.getPassword()));
        candidate.setFirstName(dto.getFirstName());
        candidate.setLastName(dto.getLastName());
        candidate.setPhoneNumber(dto.getPhoneNumber());

        return candidateRepository.save(candidate);
    }

    public Optional<Candidate> authenticate(String email, String password) {
        return candidateRepository.findByEmail(email)
                .filter(candidate -> passwordEncoder.matches(password, candidate.getPassword()));
    }

    public Candidate uploadCVByEmail(String email, MultipartFile file) throws IOException {
        Candidate candidate = candidateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        byte[] fileBytes = file.getBytes();
        String extractedText = CVParserUtil.extractText(fileBytes);

        candidate.setCvFile(fileBytes);
        candidate.setCvText(extractedText);

        return candidateRepository.save(candidate);
    }

    public Optional<Candidate> getCandidateProfile(Long candidateId) {
        return candidateRepository.findById(candidateId);
    }
}


