package com.example.ecommerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddCartItemRequest(
        @NotNull Long productId,
        @NotNull @Min(value = 1, message = "Quantity must be at least 1") Integer quantity
) {
}
