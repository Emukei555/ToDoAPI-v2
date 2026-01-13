package com.sqlcanvas.todoapi.order.domain.model.payment;

// 1. クレジットカード (手数料0円)
public record CreditCard(String number, String expiry) implements PaymentMethod {
    @Override
    public long calculateFee(long amount) {
        return 0; // 手数料なし
    }
}