package com.cours.app.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hachage des mots de passe.
 *
 * <p>Pour rester simple en cours, on utilise SHA-256 (le mot de passe n'est
 * jamais stocké en clair). Dans une vraie application, on préférerait un
 * algorithme dédié comme bcrypt/argon2 avec un "salt".</p>
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    public static String hash(String raw) {
        if (raw == null) {
            raw = "";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }

    /** Compare un mot de passe saisi à un hash stocké. */
    public static boolean matches(String raw, String storedHash) {
        if (storedHash == null) {
            return false;
        }
        return hash(raw).equalsIgnoreCase(storedHash);
    }
}
