package navik.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * Amazon S3(Simple Storage Service) 관련 설정을 담당하는 클래스입니다.
 * AWS SDK for Java v2를 사용하여 S3 Presigner를 설정합니다.
 */
@Configuration
@ConditionalOnProperty(name = "spring.cloud.aws.s3.enabled", havingValue = "true")
public class S3Config {

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    /**
     * Create an S3Presigner Spring bean for generating pre-signed S3 URLs.
     *
     * Configures the presigner with the application's AWS region and the SDK's default credentials provider.
     *
     * @return an S3Presigner configured with the application's AWS region and DefaultCredentialsProvider
     */
    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}