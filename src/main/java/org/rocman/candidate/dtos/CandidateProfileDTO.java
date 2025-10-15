package org.rocman.candidate.dtos;

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

    private List<EducationDTO> education = new ArrayList<>();
    private List<ExperienceDTO> experience = new ArrayList<>();
    private List<SkillDTO> skill = new ArrayList<>();
    private List<LanguageDTO> language = new ArrayList<>();

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