package com.sqlcanvas.todoapi.order.domain.model;

import com.sqlcanvas.todoapi.order.OrderStatus;
import com.sqlcanvas.todoapi.order.domain.model.payment.CashOnDelivery;
import com.sqlcanvas.todoapi.order.domain.model.payment.CreditCard;
import com.sqlcanvas.todoapi.order.domain.model.payment.PayPay;
import com.sqlcanvas.todoapi.order.domain.model.payment.PaymentMethod;
import com.sqlcanvas.todoapi.user.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter // GetterはOKだが、Setterは禁止！
@NoArgsConstructor // JPAのために必要（protected推奨だがpublicでも可）
@ToString
@Slf4j
public class Order {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "order")
    private List<OrderItem> items = new ArrayList<>();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // ここを修正！
    @Column(nullable = false)
    private Long totalAmount = 0L;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;
    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;
    @Column(nullable = false)
    private Long paymentFee = 0L; // 手数料（初期値0）


    // ★コンストラクタで「必須項目」と「初期状態」を強制する
    // Effective Java 項目1「インスタンス化の制御」
    public Order(User user) { // ここを修正！
        if (user == null) {
            throw new IllegalArgumentException("注文にはユーザーが必須です");
        }
        this.user = user;
        this.orderDate = LocalDateTime.now();
        this.status = OrderStatus.PENDING; // 初期状態
        this.orderDate = LocalDateTime.now(); // 作成日時
        this.totalAmount = 0L;
    }

    public void addItem(OrderItem item) {
        // 1. ガード節（メソッド抽出）
        validateAddItem(item);

        // 2. 関連付け
        items.add(item);
        item.setOrder(this);

        // 3. 再計算（足し込むのではなく、リスト全体から計算し直す）
        recalculateTotal();
    }

    // ★課題だった「メソッド抽出」
    private void validateAddItem(OrderItem item) {
        if (item == null) {
            throw new IllegalArgumentException("追加する商品がnullです");
        }
        // ルール: 1注文につき30アイテムまで
        if (this.items.size() >= 30) {
            throw new IllegalStateException("1回の注文で追加できるのは30アイテムまでです");
        }
    }

    public void cancel() {
        if (this.status == OrderStatus.PENDING) {
            throw new IllegalStateException("支払い済みの注文はキャンセルできません（返金処理が必要です）。");
        }
        this.status = OrderStatus.CANCELLED;
    }

    // ★安全な計算ロジック
    private void recalculateTotal() {
        // 現在のリストの中身を全部足し合わせる（これならズレない！）
        this.totalAmount = items.stream()
                .mapToLong(OrderItem::calculateSubTotal) // OrderItem側にこのメソッドが必要
                .sum();

        // 手数料も加算済みなら足す（今回は簡易的に fee を足す）
        this.totalAmount += this.paymentFee;
    }

    // ... (pay, cancel メソッドなどはそのまま) ...

    public void setPaymentMethod(PaymentMethod method) {
        // 手数料計算
        this.paymentFee = method.calculateFee(this.totalAmount);

        // ★ここでも再計算メソッドを呼ぶだけで済む
        recalculateTotal();

        // ログ出力 (Java 21 Switch - 素晴らしい実装です！)
        String logMessage = switch (method) {
            case CreditCard c -> "クレカ払い: " + c.number().substring(12);
            case CashOnDelivery c -> "代引き";
            case PayPay p -> "PayPay: " + p.accountId();
        };
        log.info("支払い設定: {}", logMessage);
    }
}