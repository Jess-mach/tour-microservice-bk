package br.com.tourapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final String bucketName;
    private final String region;

    public S3Service(@Value("${app.aws.access-key}") String accessKey,
                     @Value("${app.aws.secret-key}") String secretKey,
                     @Value("${app.aws.region}") String region,
                     @Value("${app.aws.s3.bucket-name}") String bucketName) {
        this.region = region;
        this.bucketName = bucketName;

        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }

    public String upload(MultipartFile file) {
        try {
            String fileName = generateFileName(file.getOriginalFilename());
            String key = "tourapp/excursoes/" + fileName;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao fazer upload da imagem", e);
        }
    }

    public List<String> uploadMultiplas(List<MultipartFile> files) {
        return files.stream()
                .map(this::upload)
                .collect(Collectors.toList());
    }

    public void deletar(String imageUrl) {
        try {
            String key = extractKeyFromUrl(imageUrl);
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            // Log error but don't throw - deletion failure shouldn't break the flow
            System.err.println("Erro ao deletar imagem: " + e.getMessage());
        }
    }

    private String generateFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return UUID.randomUUID().toString() + "." + extension;
    }

    private String getFileExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf(".") + 1);
        }
        return "jpg";
    }

    private String extractKeyFromUrl(String imageUrl) {
        // Extract key from S3 URL
        // Format: https://bucket.s3.region.amazonaws.com/key
        String[] parts = imageUrl.split("/");
        if (parts.length >= 4) {
            return String.join("/", java.util.Arrays.copyOfRange(parts, 3, parts.length));
        }
        return imageUrl;
    }
}