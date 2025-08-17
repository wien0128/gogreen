package com.backend.gogreen.api.member.jwt.util;

import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;

public class PasswordUtil {

    public static String generateRandomPassword() {
        PasswordGenerator generator = new PasswordGenerator();
        CharacterRule lowerCaseRule = new CharacterRule(EnglishCharacterData.LowerCase, 2);
        CharacterRule upperCaseRule = new CharacterRule(EnglishCharacterData.UpperCase, 2);
        CharacterRule numberRule = new CharacterRule(EnglishCharacterData.Digit, 2);
        CharacterRule specialRule = new CharacterRule(EnglishCharacterData.Special, 2);

        return generator.generatePassword(10, lowerCaseRule, upperCaseRule, numberRule, specialRule);
    }
}
