package org.rocman.candidate.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.rocman.candidate.dtos.CandidateProfileDTO;
import org.rocman.candidate.entities.*;

@Mapper(componentModel = "spring")
public interface CandidateMapper {

    CandidateMapper INSTANCE = Mappers.getMapper(CandidateMapper.class);


    CandidateProfileDTO toDto(Candidate candidate);

    CandidateProfileDTO.EducationDTO educationToDto(Education education);

    CandidateProfileDTO.ExperienceDTO experienceToDto(Experience experience);

    CandidateProfileDTO.SkillDTO skillToDto(Skill skill);

    CandidateProfileDTO.LanguageDTO languageToDto(Language language);


    Candidate toEntity(CandidateProfileDTO dto);

    Education educationDtoToEntity(CandidateProfileDTO.EducationDTO dto);

    Experience experienceDtoToEntity(CandidateProfileDTO.ExperienceDTO dto);

    Skill skillDtoToEntity(CandidateProfileDTO.SkillDTO dto);

    Language languageDtoToEntity(CandidateProfileDTO.LanguageDTO dto);
}