//package com.sqlcanvas.todoapi.order.domain.model;
//
//import com.sqlcanvas.todoapi.product.Price;
//import net.jqwik.api.*;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class OrderPropertyTest {
//
//    // ✅ プロパティテスト: 「どんな商品リストを追加しても、合計金額は正しく計算されるはず」
//    @Property
//    void totalAmountShouldMatchSumOfItemPrices(@ForAll("validItems") List<OrderItem> items) {
//        // Arrange
//        Order order = new Order(); // ユーザーは一旦nullでも計算ロジックには影響しないと仮定
//
//        // Act: 生成されたランダムなアイテムを全て追加
//        for (OrderItem item : items) {
//            order.addItem(item);
//        }
//
//        // 期待値の計算（テストコード側で愚直に計算）
//        long expectedTotal = items.stream()
//                .mapToLong(item -> item.getPrice().getValue() * item.getQuantity())
//                .sum();
//
//        // Assert
//        assertThat(order.getTotalAmount()).isEqualTo(expectedTotal);
//    }
//
//    // 🎲 ランダムデータ生成器 (Arbitrary)
//    // 「ありえないデータ（マイナスの価格など）」が混ざるとテストにならないので、
//    // ビジネスルールとして正しい範囲のデータを生成するように定義します。
//    @Provide
//    Arbitrary<List<OrderItem>> validItems() {
//        Arbitrary<OrderItem> itemArbitrary = Combinators.combine(
//                Arbitraries.longs().between(100, 100000),
//                Arbitraries.integers().between(1, 10)
//        ).as((priceValue, qty) -> { // 変数名を priceValue とかにすると分かりやすい
//            OrderItem item = new OrderItem();
//            item.setPrice(new Price(priceValue));
//            item.setQuantity(qty);
//            return item;
//        });
//
//        return itemArbitrary.list().ofMinSize(0).ofMaxSize(20);
//    }
//}