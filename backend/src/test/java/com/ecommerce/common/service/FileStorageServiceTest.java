package com.ecommerce.common.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.ecommerce.common.config.CloudinaryProperties;
import com.ecommerce.common.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileStorageServiceTest {

    @Test
    void storeProductImageShouldUploadToCloudinaryAndReturnSecureUrl() throws IOException {
        Cloudinary cloudinary = mock(Cloudinary.class);
        Uploader uploader = mock(Uploader.class);
        FileStorageService service = new FileStorageService(
                cloudinary,
                new CloudinaryProperties("demo", "api-key", "api-secret", "ecommerce/products")
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "keyboard.png",
                "image/png",
                "image-content".getBytes()
        );
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap()))
                .thenReturn(Map.of("secure_url", "https://res.cloudinary.com/demo/image/upload/product-1.png"));

        String imageUrl = service.storeProductImage(1L, file);

        assertThat(imageUrl).isEqualTo("https://res.cloudinary.com/demo/image/upload/product-1.png");
        verify(uploader).upload(any(byte[].class), anyMap());
    }

    @Test
    void storeProductImageShouldRejectInvalidFileType() {
        FileStorageService service = new FileStorageService(
                mock(Cloudinary.class),
                new CloudinaryProperties("demo", "api-key", "api-secret", "ecommerce/products")
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
                mock(Cloudinary.class),
                new CloudinaryProperties("demo", "api-key", "api-secret", "ecommerce/products")
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

    @Test
    void storeProductImageShouldRejectMissingCloudinaryUrl() throws IOException {
        Cloudinary cloudinary = mock(Cloudinary.class);
        Uploader uploader = mock(Uploader.class);
        FileStorageService service = new FileStorageService(
                cloudinary,
                new CloudinaryProperties("demo", "api-key", "api-secret", "ecommerce/products")
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "keyboard.png",
                "image/png",
                "image-content".getBytes()
        );
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(Map.of());

        assertThatThrownBy(() -> service.storeProductImage(1L, file))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Cloudinary upload did not return an image URL");
    }
}
