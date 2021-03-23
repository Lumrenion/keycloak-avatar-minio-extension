package com.github.thomasdarimont.keycloak.avatar.storage.minio;

import com.github.thomasdarimont.keycloak.avatar.AvatarAdminResource;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

@JBossLog
public class MinioAvatarStorageProviderFactory {
    //TODO remove default settings
    private static final String DEFAULT_SERVER_URL = "http://avatarsdb:9000";
    private static final String DEFAULT_ACCESS_KEY = "AKIAIOSFODNN7EXAMPLE";
    private static final String DEFAULT_SECRET_KEY = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";

    private static MinioConfig minioConfig;

    public static MinioAvatarStorageProvider create() {
        if (minioConfig == null) {
            init();
        }
        return new MinioAvatarStorageProvider(minioConfig);
    }

    public static void init() {
        String serverUrl = System.getenv("MINIO_SERVER_URL");
        String accessKey = System.getenv("MINIO_ACCESS_KEY");
        String secretKey = System.getenv("MINIO_SECRET_KEY");
        //String serverUrl = DEFAULT_SERVER_URL;
        //String accessKey = DEFAULT_ACCESS_KEY;
        //String secretKey = DEFAULT_SECRET_KEY;

        minioConfig = new MinioConfig(serverUrl, accessKey, secretKey);
    }
}
