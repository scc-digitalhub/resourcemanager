package it.smartcommunitylab.resourcemanager.provider.minio.crypto;

import java.nio.charset.Charset;
import java.security.MessageDigest;

import uk.co.lucasweb.aws.v4.signer.SigningException;
import uk.co.lucasweb.aws.v4.signer.functional.Throwables;

public class MinioSha256 {

    private static final char[] hexDigits = "0123456789abcdef".toCharArray();

    public static String get(byte[] bytes) {
        return Throwables.returnableInstance(() -> {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(bytes);
            md.getDigestLength();
            return bytesToHex(md.digest());
        }, SigningException::new);
    }

    public static String get(String value, Charset charset) {
        return Throwables.returnableInstance(() -> {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = value.getBytes(charset);
            md.update(bytes);
            md.getDigestLength();
            return bytesToHex(md.digest());
        }, SigningException::new);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            sb.append(hexDigits[(b >> 4) & 0xf]).append(hexDigits[b & 0xf]);
        }
        return sb.toString();
    }
}
