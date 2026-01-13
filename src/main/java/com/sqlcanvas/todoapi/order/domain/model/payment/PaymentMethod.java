package com.sqlcanvas.todoapi.order.domain.model.payment;

// domain/model/payment/PaymentMethod.java

// 支払い方法インターフェース（これらを実装できるのはpermitsされたものだけ）
public sealed interface PaymentMethod permits CreditCard, CashOnDelivery, PayPay {
    long calculateFee(long amount); // 手数料計算メソッド
}

