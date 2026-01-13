package com.sqlcanvas.todoapi.order;

import com.sqlcanvas.todoapi.order.domain.model.Order;
import com.sqlcanvas.todoapi.order.infrastructure.dto.OrderItemResponse;
import com.sqlcanvas.todoapi.order.infrastructure.dto.OrderResponse;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {
    public OrderResponse toResponse(Order order) {
        var itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProduct().getName(),
                        item.getQuantity(),
                        Math.toIntExact(item.getPrice().intValue())
                ))
                .toList();

        // 注文全体の変換
        return new OrderResponse(
                order.getId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getOrderDate(),
                itemResponses
        );
    }
}
