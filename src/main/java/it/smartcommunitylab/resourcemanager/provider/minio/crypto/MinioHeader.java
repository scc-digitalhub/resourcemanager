package it.smartcommunitylab.resourcemanager.provider.minio.crypto;

public interface MinioHeader {
    public byte getVersion();

    public byte getCipher();

    public int getLength();

    public boolean isFinal();

    public byte[] getNonce();

    public byte[] getAddData();

    public byte[] getRaw();

    public void setCipher(byte b);

    public void setLength(int i);

    public void setFinal(boolean b);

    public void setNonce(byte[] b);

    public void clear();

}
