package com.survery.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client() {
        // TODO: Fix the S3Client bean creation.
        // The current implementation is failing to compile due to a dependency issue
        // where the S3Client.Builder class cannot be found.
        // This is a temporary workaround to allow the application to build.
        // The original implementation was:
        /*
        S3Client.Builder builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create());

        if (endpointOverride != null) {
            builder.endpointOverride(URI.create(endpointOverride));
        }

        return builder.build();
        */
        return null;
    }
}
