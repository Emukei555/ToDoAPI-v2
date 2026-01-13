package com.sqlcanvas.todoapi.order.domain.model;

import com.sqlcanvas.todoapi.product.Price;
import com.sqlcanvas.todoapi.product.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
// @Setter ← 削除しました。値の変更は意味のあるメソッド経由で行います。
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false) // PriceはValueObjectだと思うので @Embedded 推奨（構成によります）
    @AttributeOverride(name = "value", column = @Column(name = "price_at_purchase", nullable = false))
    private BigDecimal price;

    // コンストラクタをprivateにして、生成はファクトリーメソッドに任せる
    private OrderItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.price = product.getPrice();
    }

    // === ここから実習 ===

    // ファクトリーメソッド
    public static OrderItem create(Product product, int quantity) {
        // TODO: 1. ここで「数量が正しいか」をチェックするガード節を入れたい。
        // ヒント: if文をここにダラダラ書くのではなく、下の privateメソッド (validateQuantity) を呼ぶ一行だけを書く

        validateQuantity(quantity);

        return new OrderItem(product, quantity);
    }

    // ★今回のテーマ：メソッド抽出されたバリデーションロジック
    private static void validateQuantity(int quantity) {
        // TODO: 3. 具体的なルールをここに書く
        // ルール: 「注文数は1個以上、かつ50個以下でなければならない」
        // 条件に違反していたら IllegalArgumentException を投げる

        if (quantity < 1 || quantity > 50) {
            throw new IllegalArgumentException("注文数は1個以上、かつ50個以下でなければなりません");
        }
    }

    // ビジネスロジック: 小計の計算
    // ヒント: Priceクラスの中身が int value だと仮定して、 quantity と掛け算した結果を返す
    public int calculateSubTotal() {
        // TODO: 2. 「単価(price) × 数量(quantity)」を計算して返す
        return Math.toIntExact(price.intValue() * quantity);
    }

    public void setOrder(Order order) {
    }
}