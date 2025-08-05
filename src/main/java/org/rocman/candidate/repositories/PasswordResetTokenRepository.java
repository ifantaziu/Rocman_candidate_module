package org.rocman.candidate.repositories;

import org.rocman.candidate.entities.Candidate;
import org.rocman.candidate.entities.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByCandidate(Candidate candidate);
}
