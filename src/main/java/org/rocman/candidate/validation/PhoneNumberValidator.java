package org.rocman.candidate.validation;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.rocman.candidate.dtos.CandidateRegistrationDTO;

public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, CandidateRegistrationDTO> {

    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    @Override
    public boolean isValid(CandidateRegistrationDTO dto, ConstraintValidatorContext context) {
        if (dto.getCallingCode() == null || dto.getPhoneNumber() == null) {
            return false;
        }

        String fullNumber = dto.getCallingCode() + dto.getPhoneNumber();

        try {
            Phonenumber.PhoneNumber parsedNumber = phoneUtil.parse(fullNumber, null);
            return phoneUtil.isValidNumber(parsedNumber);
        } catch (NumberParseException e) {
            return false;
        }
    }
}

