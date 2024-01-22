package com.ka9mal6t.vws;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AesEncryptor {
    public static String encrypt(String message, String key) throws Exception {

        byte[] textBytes = message.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = hexStringToByteArray(key);
        byte[] encryptedBytes = aesEncrypt(textBytes, keyBytes);
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private static byte[] aesEncrypt(byte[] plainText, byte[] key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        return cipher.doFinal(plainText);
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

}
