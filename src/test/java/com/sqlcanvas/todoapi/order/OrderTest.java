package com.sqlcanvas.todoapi.order;

import com.sqlcanvas.todoapi.order.domain.model.Order;
import com.sqlcanvas.todoapi.user.domain.Email;
import com.sqlcanvas.todoapi.user.domain.Rank;
import com.sqlcanvas.todoapi.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {

    @Test
    @DisplayName("注文を作成できる")
    void create_order_test() {
        // 1. 準備：Userを作るにはRankが必要なので先に作る
        // (テスト用なので適当な値でOK)
        Rank regularRank = new Rank("REGULAR", "通常会員", 0, 0);

        // 2. 準備：古いUserEntityではなく、新しいUserを作る
        // 引数を4つにする（name, email, password, rank）
        User user = new User("テストユーザー", new Email("test@example.com"), "password123", regularRank);

        // 3. 実行：新しいUserを渡してOrderを作る
        Order order = new Order(user);

        // 4. 検証
        assertThat(order.getUser()).isEqualTo(user);
        assertThat(order.getOrderDate()).isNotNull();
    }
}