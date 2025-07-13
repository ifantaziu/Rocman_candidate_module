package org.rocman.candidate.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.rocman.candidate.validation.ValidEmail;

@Data
public class CandidateRegistrationDTO {

    @NotBlank(message = "Email is required")
    @ValidEmail(message = "Email must be valid (e.g., example@domain.com)")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must have at least 8 characters")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*]).+$",
            message = "Password must contain at least one uppercase letter, one digit, and one special character")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^0[0-9]{8}$",
            message = "Phone number must start with 0 and have 9 digits"
    )
    private String phoneNumber;
}