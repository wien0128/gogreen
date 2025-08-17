package com.backend.gogreen.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.passay.*;

import java.util.Arrays;

// 비밀번호 검증기
public class CustomPasswordValidator implements ConstraintValidator<Password, String> {

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {

        if (s == null) return false;

        PasswordValidator validator = new PasswordValidator(Arrays.asList(
                // 최소 8자, 최대 16자
                new LengthRule(8, 16),
                // 영문 대문자 1개 이상
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                // 영문 소문자 1개 이상
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                // 숫자 1개 이상
                new CharacterRule(EnglishCharacterData.Digit, 1),
                // 특수문자 1개 이상
                new CharacterRule(EnglishCharacterData.Special, 1),
                // 연속되는 문자 금지
                new RepeatCharacterRegexRule(2)
        ));

        // 입력 비밀번호가 정책에 맞는지 검증
        // true면 검증 성공
        RuleResult result = validator.validate(new PasswordData(s));
        if (result.isValid()) {
            return true;
        }

        // 실패 메시지 커스터마이징
        String message = String.join(", ", validator.getMessages(result));
        constraintValidatorContext.disableDefaultConstraintViolation();

        constraintValidatorContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();

        return false;
    }
}
