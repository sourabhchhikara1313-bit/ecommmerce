package com.example.ecommerce.dto;

import java.math.BigDecimal;

public record CartItemResponse(
        Long productId,
        String name,
        String imageUrl,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal,
        int stockAvailable,
        boolean available
) {
}
