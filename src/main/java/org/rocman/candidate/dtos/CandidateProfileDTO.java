package org.rocman.candidate.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class CandidateProfileDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String address;

    private List<EducationDTO> educations = new ArrayList<>();
    private List<ExperienceDTO> experiences = new ArrayList<>();
    private List<SkillDTO> skills = new ArrayList<>();
    private List<LanguageDTO> languages = new ArrayList<>();

    public CandidateProfileDTO(Long id, String firstName, String lastName,
                               String email, String phoneNumber, String address) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }
}