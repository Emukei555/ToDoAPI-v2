package com.sqlcanvas.todoapi.order.domain.model.payment;

// 2. 代引き (手数料330円〜)
public record CashOnDelivery() implements PaymentMethod {
    @Override
    public long calculateFee(long amount) {
        return amount < 10000 ? 330 : 440; // 金額によって変動
    }
}