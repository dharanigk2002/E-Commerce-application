package com.ecommerce.common.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ecommerce.common.config.CloudinaryProperties;
import com.ecommerce.common.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final Cloudinary cloudinary;
    private final CloudinaryProperties cloudinaryProperties;

    public FileStorageService(Cloudinary cloudinary, CloudinaryProperties cloudinaryProperties) {
        this.cloudinary = cloudinary;
        this.cloudinaryProperties = cloudinaryProperties;
    }

    public String storeProductImage(Long productId, MultipartFile file) {
        validateImage(file);

        Map<?, ?> uploadResult;
        try {
            uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", cloudinaryProperties.productImageFolder(),
                            "public_id", "product-" + productId + "-" + UUID.randomUUID(),
                            "resource_type", "image",
                            "overwrite", true
                    )
            );
        } catch (IOException exception) {
            throw new BadRequestException("Could not read product image");
        } catch (RuntimeException exception) {
            throw new BadRequestException("Could not upload product image");
        }

        Object secureUrl = uploadResult.get("secure_url");
        if (secureUrl == null) {
            throw new BadRequestException("Cloudinary upload did not return an image URL");
        }

        return secureUrl.toString();
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
}
