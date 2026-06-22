package com.ecommerce.common.service;

import com.ecommerce.common.config.UploadProperties;
import com.ecommerce.common.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Map<String, String> EXTENSIONS_BY_CONTENT_TYPE = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    private static final Set<String> ALLOWED_CONTENT_TYPES = EXTENSIONS_BY_CONTENT_TYPE.keySet();

    private final UploadProperties uploadProperties;

    public FileStorageService(UploadProperties uploadProperties) {
        this.uploadProperties = uploadProperties;
    }

    public String storeProductImage(Long productId, MultipartFile file) {
        validateImage(file);

        try {
            Path uploadDirectory = Path.of(uploadProperties.productImageDirectory())
                    .toAbsolutePath()
                    .normalize();
            Files.createDirectories(uploadDirectory);

            String filename = buildFilename(productId, file);
            Path destination = uploadDirectory.resolve(filename).normalize();

            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            return uploadProperties.productImageUrlPrefix() + "/" + filename;
        } catch (IOException exception) {
            throw new BadRequestException("Could not store product image");
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Product image is required");
        }

        String contentType = file.getContentType();
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException("Only JPEG, PNG, and WEBP images are allowed");
        }
    }

    private String buildFilename(Long productId, MultipartFile file) {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        String extension = EXTENSIONS_BY_CONTENT_TYPE.get(file.getContentType());

        if (StringUtils.hasText(originalFilename) && originalFilename.contains(".")) {
            String originalExtension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            if (Set.of(".jpg", ".jpeg", ".png", ".webp").contains(originalExtension)) {
                extension = originalExtension.equals(".jpeg") ? ".jpg" : originalExtension;
            }
        }

        return "product-" + productId + "-" + UUID.randomUUID() + extension;
    }
}
