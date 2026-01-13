package com.sqlcanvas.todoapi.order.infrastructure.dto;

import com.sqlcanvas.todoapi.order.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

// 注文の結果を返すための箱
public record OrderResponse(
        Long id,
        Long totalAmount,
        OrderStatus status,
        LocalDateTime orderDate,
        List<OrderItemResponse> items // 中身の明細も専用の箱に入れる
) {
}