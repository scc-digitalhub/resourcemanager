package it.smartcommunitylab.resourcemanager.provider.minio.crypto;

public interface MinioPackage {

    public byte version();

    public MinioHeader header();

    public byte[] payload();

    public byte[] cipherText();

    public byte[] nonce();

    public byte[] addData();

    public int length();

    public void clear();

    public void setCipherText(byte[] bytes);

    public byte[] build() throws MinioCryptoException;

}
