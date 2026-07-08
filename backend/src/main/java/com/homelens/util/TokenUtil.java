package com.homelens.util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Component;

@Component
public final class TokenUtil {
    private static final SecureRandom RND = new SecureRandom();

    public static String newRawToken() {
        byte[] b = new byte[32]; // 256-bit
        RND.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    public static String sha256Hex(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte x : dig) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private TokenUtil() {}
}

