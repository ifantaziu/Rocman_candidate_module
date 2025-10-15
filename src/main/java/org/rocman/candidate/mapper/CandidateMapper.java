package org.rocman.candidate.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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

    @Mapping(source = "level", target = "level")
    @Mapping(source = "institution", target = "institution")
    @Mapping(source = "period", target = "period")
    Education educationDtoToEntity(EducationDTO dto);

    @Mapping(source = "title", target = "title")
    @Mapping(source = "company", target = "company")
    @Mapping(source = "period", target = "period")
    Experience experienceDtoToEntity(ExperienceDTO dto);

    @Mapping(source = "name", target = "name")
    Skill skillDtoToEntity(SkillDTO dto);

    @Mapping(source = "language", target = "language")
    @Mapping(source = "level", target = "level")
    Language languageDtoToEntity(LanguageDTO dto);
}