package com.sqlcanvas.todoapi.order.infrastructure.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// 中身の明細用
public record OrderItemRequest(
        @NotNull(message = "商品IDは必須です")
        Long productId,

        @Min(value = 1, message = "個数は1個以上にしてください")
        @NotNull
        Integer quantity
) {
}