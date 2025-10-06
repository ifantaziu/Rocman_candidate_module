package org.rocman.candidate.repositories;


import org.rocman.candidate.dtos.*;
import org.rocman.candidate.entities.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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
    Optional<Candidate> findProfileById(@Param("id") Long id);

    @Query("""
        SELECT new org.rocman.candidate.dtos.CandidateProfileDTO(
            c.id,
            c.firstName,
            c.lastName,
            c.email,
            c.phoneNumber,
            c.address
        )
        FROM Candidate c
        WHERE c.id = :id
    """)
    Optional<CandidateProfileDTO> findProfileDtoById(@Param("id") Long id);

    @Query("""
        SELECT new org.rocman.candidate.dtos.EducationDTO(
            e.id,
            e.level,
            e.institution,
            e.period
        )
        FROM Education e
        WHERE e.candidate.id = :candidateId
    """)
    List<EducationDTO> findEducationsByCandidateId(@Param("candidateId") Long candidateId);

    @Query("""
        SELECT new org.rocman.candidate.dtos.ExperienceDTO(
            ex.id,
            ex.title,
            ex.company,
            ex.period
        )
        FROM Experience ex
        WHERE ex.candidate.id = :candidateId
    """)
    List<ExperienceDTO> findExperiencesByCandidateId(@Param("candidateId") Long candidateId);

    @Query("""
        SELECT new org.rocman.candidate.dtos.SkillDTO(
            s.id,
            s.name
        )
        FROM Skill s
        WHERE s.candidate.id = :candidateId
    """)
    List<SkillDTO> findSkillsByCandidateId(@Param("candidateId") Long candidateId);

    @Query("""
        SELECT new org.rocman.candidate.dtos.LanguageDTO(
            l.id,
            l.language,
            l.level
        )
        FROM Language l
        WHERE l.candidate.id = :candidateId
    """)
    List<LanguageDTO> findLanguagesByCandidateId(@Param("candidateId") Long candidateId);
}