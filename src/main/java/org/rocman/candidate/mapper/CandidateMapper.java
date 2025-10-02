package org.rocman.candidate.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.rocman.candidate.dtos.CandidateProfileDTO;
import org.rocman.candidate.entities.*;

@Mapper(componentModel = "spring")
public interface CandidateMapper {

    CandidateProfileDTO toDto(Candidate candidate);

    CandidateProfileDTO.EducationDTO educationToDto(Education education);

    CandidateProfileDTO.ExperienceDTO experienceToDto(Experience experience);

    CandidateProfileDTO.SkillDTO skillToDto(Skill skill);

    CandidateProfileDTO.LanguageDTO languageToDto(Language language);

    Candidate toEntity(CandidateProfileDTO dto);

    @Mapping(source = "level", target = "level")
    @Mapping(source = "institution", target = "institution")
    @Mapping(source = "period", target = "period")
    Education educationDtoToEntity(CandidateProfileDTO.EducationDTO dto);

    @Mapping(source = "title", target = "title")
    @Mapping(source = "company", target = "company")
    @Mapping(source = "period", target = "period")
    Experience experienceDtoToEntity(CandidateProfileDTO.ExperienceDTO dto);

    @Mapping(source = "name", target = "name")
    Skill skillDtoToEntity(CandidateProfileDTO.SkillDTO dto);

    @Mapping(source = "language", target = "language")
    @Mapping(source = "level", target = "level")
    Language languageDtoToEntity(CandidateProfileDTO.LanguageDTO dto);
}