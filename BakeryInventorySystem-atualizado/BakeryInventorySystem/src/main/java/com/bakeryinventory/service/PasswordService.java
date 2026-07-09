package com.bakeryinventory.service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordService {
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;

    private PasswordService() {
    }

    public record HashResult(String hash, String salt) {
    }

    public static HashResult hashPassword(String password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return new HashResult(hash(password, salt), Base64.getEncoder().encodeToString(salt));
    }

    public static boolean verifyPassword(String password, String expectedHash, String saltBase64) {
        byte[] salt = Base64.getDecoder().decode(saltBase64);
        String actualHash = hash(password, salt);
        return constantTimeEquals(actualHash, expectedHash);
    }

    private static String hash(String password, byte[] salt) {
        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] encoded = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(encoded);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new IllegalStateException("Nao foi possivel proteger a senha.", ex);
        }
    }

    private static boolean constantTimeEquals(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        byte[] a = left.getBytes();
        byte[] b = right.getBytes();
        int diff = a.length ^ b.length;
        for (int i = 0; i < Math.min(a.length, b.length); i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
}
