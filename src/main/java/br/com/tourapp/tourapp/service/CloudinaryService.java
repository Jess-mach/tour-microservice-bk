package br.com.tourapp.tourapp.service;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(@Value("${app.cloudinary.cloud-name}") String cloudName,
                             @Value("${app.cloudinary.api-key}") String apiKey,
                             @Value("${app.cloudinary.api-secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    public String upload(MultipartFile file) {
        try {
            Map<String, Object> options = ObjectUtils.asMap(
                    "folder", "tourapp/excursoes",
                    "resource_type", "image",
                    "format", "webp",
                    "quality", "auto:good"
            );

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            return (String) uploadResult.get("secure_url");

        } catch (IOException e) {
            throw new RuntimeException("Erro ao fazer upload da imagem", e);
        }
    }

    public List<String> uploadMultiplas(List<MultipartFile> files) {
        return files.stream()
                .map(this::upload)
                .collect(Collectors.toList());
    }

    public void deletar(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            // Log error but don't throw - deletion failure shouldn't break the flow
            System.err.println("Erro ao deletar imagem: " + e.getMessage());
        }
    }
}
