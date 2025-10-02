package org.rocman.candidate.dtos;

import lombok.Data;

import java.util.List;

@Data
public class CandidateProfileDTO {
    private Long id;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private String address;

    private List<EducationDTO> education;
    private List<ExperienceDTO> experience;
    private List<SkillDTO> skills;
    private List<LanguageDTO> languages;

    @Data
    public static class EducationDTO {
        private Long id;
        private String level;
        private String institution;
        private String period;
    }

    @Data
    public static class ExperienceDTO {
        private Long id;
        private String title;
        private String company;
        private String period;
    }

    @Data
    public static class SkillDTO {
        private Long id;
        private String name;
    }

    @Data
    public static class LanguageDTO {
        private Long id;
        private String language;
        private String level;
    }
}
