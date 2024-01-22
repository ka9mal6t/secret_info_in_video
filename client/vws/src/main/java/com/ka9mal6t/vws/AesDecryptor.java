package com.ka9mal6t.vws;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AesDecryptor {
    public static String decrypt(String ciphertext, String key) throws Exception {
        byte[] cipherText = Base64.getDecoder().decode(ciphertext);
        byte[] keyBytes = hexStringToByteArray(key);
        byte[] decryptedBytes = aesDecrypt(cipherText, keyBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);

    }

    private static byte[] aesDecrypt(byte[] cipherText, byte[] key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        return cipher.doFinal(cipherText);
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
