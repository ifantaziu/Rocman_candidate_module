package org.rocman.candidate.repositories;

import org.rocman.candidate.entities.Candidate;
import org.rocman.candidate.entities.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByCandidate(Candidate candidate);

    long countByCandidateAndCreatedAtAfter(Candidate candidate, LocalDateTime after);
}
