package com.xperience.hero.util;

import java.security.SecureRandom;
import java.util.UUID;

public class TokenGenerator {
    
    private static final SecureRandom random = new SecureRandom();
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    
    public static String generateSecureToken() {
        return UUID.randomUUID().toString() + "-" + generateRandomString(32);
    }
    
    private static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}
