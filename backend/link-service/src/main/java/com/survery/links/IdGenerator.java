package com.survery.links;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.stream.Collectors;

public class IdGenerator {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int DEFAULT_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a random Base62 string of a default length (8).
     * @return A random Base62 string.
     */
    public static String generate() {
        return generate(DEFAULT_LENGTH);
    }

    /**
     * Generates a random Base62 string of the specified length.
     * @param length The desired length of the string.
     * @return A random Base62 string.
     */
    public static String generate(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive.");
        }

        return RANDOM.ints(length, 0, CHARS.length())
                     .mapToObj(CHARS::charAt)
                     .map(Object::toString)
                     .collect(Collectors.joining());
    }
}
