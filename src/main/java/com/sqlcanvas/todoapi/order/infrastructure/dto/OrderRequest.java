package com.sqlcanvas.todoapi.order.infrastructure.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// 注文リクエスト全体
public record OrderRequest(

        @NotNull(message = "ユーザーIDは必須です")
        Long userId,

        @NotEmpty(message = "商品は最低1つ必要です")
        @Valid
        List<OrderItemRequest> items
) {
}