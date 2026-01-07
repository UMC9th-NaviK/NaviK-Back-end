package navik.global.s3.service;

import navik.global.s3.dto.S3Dto;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

/**
 * Amazon S3 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.cloud.aws.s3.enabled", havingValue = "true")
public class S3Service {

    private final S3Presigner s3Presigner;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * Generate a presigned S3 PUT URL for uploading a file.
     *
     * The returned response contains the presigned URL (valid for 10 minutes) and the S3 object key.
     * The object key is generated as "{prefix}/{UUID}_{fileName}".
     *
     * @param prefix   the S3 folder path (key prefix) where the file will be stored
     * @param fileName the original name of the file to be uploaded
     * @return a {@link S3Dto.PreSignedUrlResponse} containing the presigned URL and the generated object key
     */
    public S3Dto.PreSignedUrlResponse getPreSignedUrl(String prefix, String fileName) {
        String key = prefix + "/" + UUID.randomUUID() + "_" + fileName;

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return S3Dto.PreSignedUrlResponse.builder()
                .preSignedUrl(presignedRequest.url().toString())
                .key(key)
                .build();
    }
}