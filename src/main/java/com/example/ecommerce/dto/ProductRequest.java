package com.example.ecommerce.dto;

import jakarta.validation.constraints.*;
import lombok.NonNull;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 2000) String description,
        @NotNull @DecimalMin(value = "0.01", message = "Price must be at least 0.01") BigDecimal price,
        @NotNull @Min(value = 0, message = "Stock cannot be negative") Integer stockQuantity,
        @Size(max = 500) String imageUrl,
        @NotNull Long categoryId
        ) {
}
