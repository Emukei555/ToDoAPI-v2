package com.sqlcanvas.todoapi.order;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    RETURNED;
    // 必要であれば、DB値との変換ロジックを追加
}