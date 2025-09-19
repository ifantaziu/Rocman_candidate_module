package org.rocman.candidate.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.rocman.candidate.validation.ValidEmail;
import org.rocman.candidate.validation.ValidPhoneNumber;

@Data
@ValidPhoneNumber
public class CandidateRegistrationDTO {

    @NotBlank(message = "Email is required")
    @ValidEmail(message = "Email must be valid (e.g., example@domain.com)")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must have at least 8 characters")
    @Pattern(regexp = "^(?=.*[A-ZА-ЯЁ])(?=.*\\d)(?=.*[!@#$%^&*]).+$",
            message = "Password must contain at least one uppercase letter, one digit, and one special character")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Country calling code is required")
    private String callingCode;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
}