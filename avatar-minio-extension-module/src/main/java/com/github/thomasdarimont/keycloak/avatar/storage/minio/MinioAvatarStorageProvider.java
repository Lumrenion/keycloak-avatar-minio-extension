package com.github.thomasdarimont.keycloak.avatar.storage.minio;

import com.github.thomasdarimont.keycloak.avatar.AvatarAdminResource;
import lombok.extern.jbosslog.JBossLog;
import io.minio.PutObjectArgs;
import io.minio.GetObjectArgs;

import java.io.InputStream;

@JBossLog
public class MinioAvatarStorageProvider {
    
    private final MinioTemplate minioTemplate;

    public MinioAvatarStorageProvider(MinioConfig minioConfig) {
        this.minioTemplate = new MinioTemplate(minioConfig);
    }

    public void saveAvatarImage(String realmName, String userId, InputStream input) {
        String bucketName = minioTemplate.getBucketName(realmName);
        minioTemplate.ensureBucketExists(bucketName);

        minioTemplate.execute(minioClient -> {
            minioClient.putObject(
                PutObjectArgs.builder().bucket(bucketName).object(userId).stream(
                        input, -1, 10485760)
                    .contentType("image/jpeg")
                    .build());
            return null;
        });
    }

    public InputStream loadAvatarImage(String realmName, String userId) {
        String bucketName = minioTemplate.getBucketName(realmName);

        return minioTemplate.execute(minioClient -> minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(userId).build()) );
    }

    public void close() {
        // NOOP
    }


}
