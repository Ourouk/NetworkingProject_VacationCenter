package com.hepl.customHttpServer.authServerConnector;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AES {

    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/ECB/PKCS5Padding";


    public static byte[] encrypt(String message, byte[] key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key, AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(message.getBytes());
    }

    public static String decrypt(byte[] encrypted, byte[] key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key, AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted);
    }
}
