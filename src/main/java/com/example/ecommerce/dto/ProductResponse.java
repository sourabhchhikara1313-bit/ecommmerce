package com.example.ecommerce.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        int stockQuantity,
        String imageUrl,
        Long categoryId,
        String categoryName,
        boolean active
) {
}
