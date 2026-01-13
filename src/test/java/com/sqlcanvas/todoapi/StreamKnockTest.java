package com.sqlcanvas.todoapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StreamKnockTest {
    // テスト用のデータ準備メソッド
    private List<Order> getOrders() {
        return List.of(
                new Order(1, "Tanaka", 1000, OrderStatus.SHIPPED),
                new Order(2, "Suzuki", 5000, OrderStatus.PENDING),
                new Order(3, "Sato", 2000, OrderStatus.SHIPPED),
                new Order(4, "Yamada", 3000, OrderStatus.CANCELLED),
                new Order(5, "Kato", 1500, OrderStatus.PENDING)
        );
    }

    @Test
    @DisplayName("Lv.1: 発送済み(SHIPPED)の顧客名リストを作成")
    void level1_filter_map() {
        var orders = getOrders();
        // 1. SHIPPED だけ選ぶ
        // 2. 顧客名(String) だけ取り出す
        // 3. リストにする
        List<String> shippedCustomers = orders.stream()
                .filter(order -> order.status() == OrderStatus.SHIPPED)
                .map(Order::customer)
                .toList();

        assertEquals(2, shippedCustomers.size());
        assertEquals(List.of("Tanaka", "Sato"), shippedCustomers);
    }

    @Test
    @DisplayName("Lv.2: ステータスごとに注文をグループ分けする")
    void level2_grouping() {
        var orders = getOrders();

        // TODO: OrderStatus をキーにして、そのステータスの注文リストを値にする Map を作る
        Map<OrderStatus, List<Order>> ordersByStatus = orders.stream()
                // ヒント: Collectors.groupingBy(Order::status)
                .collect(Collectors.groupingBy(Order::status));
        // 検証
        // PENDING は 2件 (Suzuki, Kato) あるはず
        assertEquals(2, ordersByStatus.get(OrderStatus.PENDING).size());
        // CANCELLED は 1件 (Yamada) あるはず
        assertEquals(1, ordersByStatus.get(OrderStatus.CANCELLED).size());
    }

    @Test
    @DisplayName("Lv.3: 未発送(PENDING)の金額合計を計算")
    void level3_sum() {
        var orders = getOrders();

        // int totalAmount = ... ここにロジックを実装
        // ヒント: .filter(...) -> .mapToInt(...) -> .sum()
        int totalAmount = orders.stream()
                .filter(order -> order.status() == OrderStatus.PENDING)
                .mapToInt(Order::amount)
                .sum();

        assertEquals(6500, totalAmount);
    }

    @Test
    @DisplayName("Lv.4: 金額が高い順に並び替えて、顧客名リストを作る")
    void level4_sorted() {
        var orders = getOrders();

        // TODO: 金額(amount)が高い順（降順）に並び替え、顧客名(customer)だけのリストにする
        List<String> sortedCustomers = orders.stream()
                // ヒント1: .sorted(...) を使います
                // ヒント2: 比較ルールは Comparator.comparingInt(...) で金額を指定
                // ヒント3: そのままだと昇順(小さい順)なので、.reversed() で反転させる
                .sorted(Comparator.comparing(Order::amount).reversed())
                // 顧客名に変換
                .map(Order::customer)
                .toList();

        // 検証
        assertEquals(List.of("Suzuki", "Yamada", "Sato", "Kato", "Tanaka"), sortedCustomers);
    }

    @Test
    @DisplayName("Lv.5: 最高額の注文をした顧客を1人特定する")
    void level5_max() {
        var orders = getOrders();

        // TODO: 全注文の中で一番金額(amount)が高い注文を探し、その顧客名(customer)を返す
        String topSpender = orders.stream()
                // ヒント1: .max(...) を使います。引数は Lv.4 と同じ Comparator です
                .max(Comparator.comparingInt(Order::amount))
                // ヒント2: 結果は Optional<Order> なので、そこから顧客名(String)に変換(.map)する
                .map(Order::customer)
                // ヒント3: 最後に Optional から値を取り出す（なければ "Unknown"）
                .orElse("Unknown");

        // 検証
        assertEquals("Suzuki", topSpender);
    }

    @Test
    @DisplayName("Lv.6: Entity を DTO に変換する (実務の基本)")
    void level6_entity_to_dto() {
        // DBから取ってきたデータ（削除済みの人も混ざっている）
        var entities = List.of(
                new UserEntity(1, "tanaka", false),
                new UserEntity(2, "suzuki", true),  // 削除済み
                new UserEntity(3, "sato", false)
        );

        // TODO: 以下の処理をStreamで実装してください
        // 1. 削除済み(isDeleted == true)を除外
        // 2. UserDto に変換 (名前は大文字にする)
        // 3. リストにする
        List<UserDto> response = entities.stream()
                // .filter(...)    // ！ isDeleted が false のものだけ通す
                // .map(...)       // ！ ここで new UserDto(...) をする
                .filter(entity -> !entity.isDeleted)
                .map(e -> new UserDto(e.id, e.name.toUpperCase()))
                .toList();

        // 検証
        assertEquals(2, response.size());
        assertEquals("TANAKA", response.get(0).name()); // 大文字になってる？
        assertEquals(3, response.get(1).id());          // satoさんが2番目に来てる？
    }

    // 注文ステータス
    enum OrderStatus {PENDING, SHIPPED, CANCELLED}

    // 注文レコード (ID, 顧客名, 金額, ステータス)
    record Order(int id, String customer, int amount, OrderStatus status) {
    }

    // --- 準備：この2つを定義 ---
    // 1. データベース用 (重たいクラスのフリ)
    static class UserEntity {
        int id;
        String name;
        boolean isDeleted;

        // コンストラクタ
        UserEntity(int id, String name, boolean isDeleted) {
            this.id = id;
            this.name = name;
            this.isDeleted = isDeleted;
        }
    }

    // 2. API用 (軽いレコード)
    record UserDto(int id, String name) {
    }
}