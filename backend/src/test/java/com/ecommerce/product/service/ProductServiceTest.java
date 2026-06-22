package com.ecommerce.product.service;

import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.common.service.FileStorageService;
import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ProductService productService;

    @Test
    void createShouldSaveProductAndReturnResponse() {
        ProductRequest request = new ProductRequest(
                "Keyboard",
                "Mechanical keyboard",
                new BigDecimal("129.99"),
                10,
                true,
                "https://res.cloudinary.com/demo/image/upload/keyboard.jpg"
        );
        Product savedProduct = new Product(
                request.name(),
                request.description(),
                request.price(),
                request.availableStock(),
                request.active()
        );
        savedProduct.setImageUrl(request.imageUrl());

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        var response = productService.create(request);

        assertThat(response.name()).isEqualTo("Keyboard");
        assertThat(response.price()).isEqualByComparingTo("129.99");
        assertThat(response.availableStock()).isEqualTo(10);
        assertThat(response.imageUrl()).isEqualTo("https://res.cloudinary.com/demo/image/upload/keyboard.jpg");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void findByIdShouldThrowWhenProductDoesNotExist() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found with id: 99");
    }

    @Test
    void updateImageShouldStoreFileAndUpdateProductImageUrl() {
        Product product = new Product(
                "Keyboard",
                "Mechanical keyboard",
                new BigDecimal("129.99"),
                10,
                true
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "keyboard.png",
                "image/png",
                "image-content".getBytes()
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(fileStorageService.storeProductImage(1L, file))
                .thenReturn("https://res.cloudinary.com/demo/image/upload/product-1.png");

        var response = productService.updateImage(1L, file);

        assertThat(product.getImageUrl()).isEqualTo("https://res.cloudinary.com/demo/image/upload/product-1.png");
        assertThat(response.imageUrl()).isEqualTo("https://res.cloudinary.com/demo/image/upload/product-1.png");
    }

    @Test
    void updateImageShouldThrowWhenProductDoesNotExist() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "keyboard.png",
                "image/png",
                "image-content".getBytes()
        );

        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateImage(99L, file))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found with id: 99");
    }
}
