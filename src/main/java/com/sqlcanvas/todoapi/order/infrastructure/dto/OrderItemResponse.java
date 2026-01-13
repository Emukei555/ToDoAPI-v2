package com.sqlcanvas.todoapi.order.infrastructure.dto;

// 明細の箱
public record OrderItemResponse(
        String productName,
        Integer quantity,
        Integer priceAtPurchase
) {
}