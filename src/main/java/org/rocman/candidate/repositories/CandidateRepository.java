package org.rocman.candidate.repositories;


import org.rocman.candidate.entities.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    Optional<Candidate> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    @Query("SELECT c FROM Candidate c " +
            "LEFT JOIN FETCH c.educations " +
            "LEFT JOIN FETCH c.experiences " +
            "LEFT JOIN FETCH c.skills " +
            "LEFT JOIN FETCH c.languages " +
            "WHERE c.id = :id")
    Optional<Candidate> findByIdWithAllRelations(@Param("id") Long id);
}