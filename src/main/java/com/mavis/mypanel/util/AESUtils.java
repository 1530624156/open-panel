package com.mavis.mypanel.util;

import com.mavis.mypanel.constant.ConstantPool;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESUtils {
    private static final String AES_ALGORITHM = "AES";

    // 生成密钥
    public static String generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGen.init(128); // AES 支持 128, 192, 256 位密钥
        SecretKey secretKey = keyGen.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    // 加密
    public static String encrypt(String data, String key) throws Exception {
        // 1. 创建密钥，使用与解密一致的方式
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

        // 2. 创建加密对象，使用 AES/ECB/PKCS5Padding 模式
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        // 3. 加密数据
        byte[] encryptedBytes = cipher.doFinal(data.getBytes("UTF-8"));

        // 4. 对加密结果进行 Base64 编码后返回
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // 解密
    public static String decrypt(String encryptedData, String key) throws Exception {
        // 1. 解密前要进行 Base64 解码
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);

        // 2. 使用与前端一致的密钥进行解密
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

        // 3. 创建解密对象，使用 AES/ECB/PKCS7Padding 模式
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        // 4. 解密
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        // 5. 返回解密后的文本
        return new String(decryptedBytes, "UTF-8");
    }

    // 测试
    public static void main(String[] args) {
        try {
            //String key = generateKey(); // 生成密钥
            //System.out.println("生成的密钥: " + key);

            //String originalData = "ssh连接异常: null";
            //System.out.println("原始数据: " + originalData);
            //
            //String encryptedData = encrypt(originalData, key);
            //
            //System.out.println("加密后: " + encryptedData);
            // 解密
            //String decryptedData = decrypt("HAznCx0iy5n+rosLQDMPST7AciFxe6rL9mhKtUSqHOo=", key);
            //System.out.println("解密后: " + decryptedData);

            String decrypt = decrypt("Be0OFmULAo2CiZeMD2oqzQRXcKoWamyFrHaKzi01GxgOPJ0edBPKyFxPtvwi1cG9rXFuDawJL+KmiAmV/HMh3d+QcjRt1sa4WvPY/99DLLk=", ConstantPool.AES_PASSWORD);
            System.out.println(decrypt);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
