package com.example.bookingapp.Utils;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

public class RandomIdUtils {
    private static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private static final SecureRandom random = new SecureRandom();

    public static String generateRandomId() {
        return UUID.randomUUID().toString();
    }
    public static String generateRandomPassword(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Password length must be greater than 0");
        }

        StringBuilder password = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            password.append(
                    CHARACTERS.charAt(random.nextInt(CHARACTERS.length()))
            );
        }

        return password.toString();
    }
}
