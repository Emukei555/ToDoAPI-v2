package com.sqlcanvas.todoapi.order.domain.model.payment;

// 3. PayPay (手数料0円だが、ポイント付与ロジックなどが将来入るかも)
public record PayPay(String accountId) implements PaymentMethod {
    @Override
    public long calculateFee(long amount) {
        return 0;
    }
}
