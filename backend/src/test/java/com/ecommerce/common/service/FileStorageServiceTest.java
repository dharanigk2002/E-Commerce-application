package com.ecommerce.common.service;

import com.ecommerce.common.config.UploadProperties;
import com.ecommerce.common.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceTest {

    @TempDir
    private Path tempDir;

    @Test
    void storeProductImageShouldSaveFileAndReturnPublicUrl() {
        FileStorageService service = new FileStorageService(
                new UploadProperties(tempDir.toString(), "/uploads/products")
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "keyboard.png",
                "image/png",
                "image-content".getBytes()
        );

        String imageUrl = service.storeProductImage(1L, file);

        assertThat(imageUrl).startsWith("/uploads/products/product-1-");
        assertThat(imageUrl).endsWith(".png");
        assertThat(Files.exists(tempDir.resolve(imageUrl.substring(imageUrl.lastIndexOf("/") + 1)))).isTrue();
    }

    @Test
    void storeProductImageShouldRejectInvalidFileType() {
        FileStorageService service = new FileStorageService(
                new UploadProperties(tempDir.toString(), "/uploads/products")
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.txt",
                "text/plain",
                "not-image".getBytes()
        );

        assertThatThrownBy(() -> service.storeProductImage(1L, file))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Only JPEG, PNG, and WEBP images are allowed");
    }

    @Test
    void storeProductImageShouldRejectEmptyFile() {
        FileStorageService service = new FileStorageService(
                new UploadProperties(tempDir.toString(), "/uploads/products")
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.png",
                "image/png",
                new byte[0]
        );

        assertThatThrownBy(() -> service.storeProductImage(1L, file))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Product image is required");
    }
}
