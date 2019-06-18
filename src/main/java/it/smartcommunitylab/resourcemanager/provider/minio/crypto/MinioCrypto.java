package it.smartcommunitylab.resourcemanager.provider.minio.crypto;

public class MinioCrypto {

    public final static int SALT_SIZE = 32;

    /*
     * Factories
     */

    public static MinioDecrypter getDecrypter(String secretKey, byte[] raw) throws MinioCryptoException {
        return new MinioDecrypter(secretKey, raw, SALT_SIZE);
    }

    public static MinioCrypter getCrypter(String secretKey, String plainText) throws MinioCryptoException {
        // support only v2 + AES
        return new MinioCrypter(secretKey, plainText, SALT_SIZE, 2, (byte) 0);
    }

}
