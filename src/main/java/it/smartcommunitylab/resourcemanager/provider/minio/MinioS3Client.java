package it.smartcommunitylab.resourcemanager.provider.minio;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;

import io.minio.MinioClient;
import io.minio.Result;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import io.minio.messages.Upload;

public class MinioS3Client {

    private final static Logger _log = LoggerFactory.getLogger(MinioS3Client.class);

    private final String ENDPOINT;
    private final String ACCESS_KEY;
    private final String SECRET_KEY;

    public MinioS3Client(String host, int port, boolean ssl, String accessKey, String secretKey) {
        super();

        // build endpoint
        StringBuilder sb = new StringBuilder();
        if (ssl) {
            sb.append("https://");
        } else {
            sb.append("http://");
        }

        sb.append(host).append(":").append(port);

        ENDPOINT = sb.toString();
        ACCESS_KEY = accessKey;
        SECRET_KEY = secretKey;
    }

    private MinioClient connect() throws MinioException {
        return new MinioClient(
                ENDPOINT,
                ACCESS_KEY, SECRET_KEY);
    }

    public boolean ping() {
        try {
            MinioClient minio = connect();
            // list buckets to check status
            // TODO replace with REST api call
            minio.listBuckets();

            return true;
        } catch (Exception ex) {
            _log.error(ex.getMessage());
            return false;
        }
    }

    public boolean hasBucket(String bucketName) throws MinioException {
        MinioClient minio = connect();
        try {
            return minio.bucketExists(bucketName);
        } catch (MinioException mex) {
            _log.error(mex.getMessage());
            throw mex;
        } catch (InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException ex) {
            _log.error(ex.getMessage());
            throw new MinioException(ex.getMessage());
        }
    }

    public String createBucket(String bucketName) throws MinioException {
        MinioClient minio = connect();

        try {
            if (minio.bucketExists(bucketName)) {
                throw new MinioException("bucket exists");
            }

            _log.debug("create bucket " + bucketName);

            // make bucket is void *-*
            minio.makeBucket(bucketName);

            return bucketName;
        } catch (MinioException mex) {
            _log.error(mex.getMessage());
            throw mex;
        } catch (InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException ex) {
            _log.error(ex.getMessage());
            throw new MinioException(ex.getMessage());
        }

    }

    public void deleteBucket(String bucketName, boolean cleanup) throws MinioException {
        MinioClient minio = connect();
        try {
            if (minio.bucketExists(bucketName)) {

                // remove bucket does NOT remove the objects
                if (cleanup) {
                    _log.debug("cleanup uploads for bucket " + bucketName);

                    // could be a long run - TODO async
                    Iterable<Result<Upload>> incompleteUploads = minio.listIncompleteUploads(bucketName);
                    for (Result<Upload> result : incompleteUploads) {
                        Upload upload = result.get();
                        String name = upload.objectName();

                        _log.debug("delete upload " + name + " from bucket " + bucketName);
                        minio.removeIncompleteUpload(bucketName, name);
                    }

                    _log.debug("cleanup objects for bucket " + bucketName);

                    Iterable<Result<Item>> items = minio.listObjects(bucketName);
                    for (Result<Item> result : items) {
                        Item item = result.get();
                        String name = item.objectName();

                        _log.debug("delete object " + name + " from bucket " + bucketName);
                        minio.removeObject(bucketName, name);
                    }

                }

                _log.debug("delete bucket " + bucketName);

                // now remove bucket
                minio.removeBucket(bucketName);
            }
        } catch (MinioException mex) {
            _log.error(mex.getMessage());
            throw mex;
        } catch (InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException ex) {
            _log.error(ex.getMessage());
            throw new MinioException(ex.getMessage());
        }
    }

    // TODO
    // implement direct api calls for user management and policies

}
