package org.rocman.candidate.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.rocman.candidate.dtos.*;
import org.rocman.candidate.entities.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CandidateMapper {
    CandidateProfileDTO toDto(Candidate candidate);

    EducationDTO educationToDto(Education education);

    ExperienceDTO experienceToDto(Experience experience);

    SkillDTO skillToDto(Skill skill);

    LanguageDTO languageToDto(Language language);

    Candidate toEntity(CandidateProfileDTO dto);

    Education educationDtoToEntity(EducationDTO dto);

    Experience experienceDtoToEntity(ExperienceDTO dto);

    Skill skillDtoToEntity(SkillDTO dto);

    Language languageDtoToEntity(LanguageDTO dto);
}