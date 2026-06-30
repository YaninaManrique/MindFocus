package com.diacode.mindfocus.utils;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PasswordUtils {
    private static final int SALT_LENGTH = 16;
    /**
     * Genera un hash seguro de la contraseña, incluyendo el salt al inicio.
     * El resultado guarda TODO junto: "salt:hash" (codificado en Base64)
     * para que no necesites una columna extra en la base de datos.
     */
    public static String hashPassword(String password) {
        try {
            // 1. Generar salt aleatorio
            byte[] salt = new byte[SALT_LENGTH];
            new SecureRandom().nextBytes(salt);

            // 2. Hashear password + salt
            byte[] hash = hashWithSalt(password, salt);

            // 3. Combinar salt + hash y codificar en Base64
            byte[] combined = new byte[salt.length + hash.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hash, 0, combined, salt.length, hash.length);

            return Base64.encodeToString(combined, Base64.NO_WRAP);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al hashear la contraseña", e);
        }
    }

    /**
     * Verifica si la contraseña ingresada coincide con el hash guardado.
     */
    public static boolean verificarPassword(String passwordIngresada, String hashGuardado) {
        try {
            byte[] combined = Base64.decode(hashGuardado, Base64.NO_WRAP);

            // Extraer el salt (primeros SALT_LENGTH bytes)
            byte[] salt = new byte[SALT_LENGTH];
            System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);

            // Extraer el hash original (el resto)
            byte[] hashOriginal = new byte[combined.length - SALT_LENGTH];
            System.arraycopy(combined, SALT_LENGTH, hashOriginal, 0, hashOriginal.length);

            // Hashear la contraseña ingresada con el MISMO salt
            byte[] hashIngresado = hashWithSalt(passwordIngresada, salt);

            // Comparar
            return MessageDigest.isEqual(hashOriginal, hashIngresado);

        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] hashWithSalt(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);
        return md.digest(password.getBytes());
    }
}
