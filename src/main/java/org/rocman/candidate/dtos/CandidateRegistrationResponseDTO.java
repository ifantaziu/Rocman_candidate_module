package org.rocman.candidate.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateRegistrationResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String message;
}
