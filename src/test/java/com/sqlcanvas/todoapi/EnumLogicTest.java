package com.sqlcanvas.todoapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnumLogicTest {
    // 1. ユーザー権限
    enum Role { ADMIN, EDITOR, USER, GUEST }

    // 2. 注文ステータス
    enum Status { PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED }

    // 3. 配送プラン
    enum Shipping { STANDARD, EXPRESS, FREE }

    @Test
    @DisplayName("Lv.1: 権限に応じた日本語ラベルを取得")
    void level1_label() {
        Role role = Role.ADMIN;

        String label = switch (role) {
            case ADMIN -> "管理者";
            case EDITOR -> "編集者";
            case USER -> "ユーザー";
            case GUEST -> "ゲスト";
        };
        assertEquals("管理者", label);
    }

    @Test
    @DisplayName("Lv.2: 注文キャンセルが可能か判定する")
    void level2_can_cancel() {
        Status currentStatus = Status.SHIPPED;
        // TODO: キャンセル可能かどうかを判定してください
        boolean canCancel = switch (currentStatus) {
            // ヒント: キャンセルできるステータスを列挙
            case PENDING, PROCESSING -> true;
            case SHIPPED, DELIVERED, CANCELLED -> false;
        };
        assertEquals(false, canCancel);
    }

    @Test
    @DisplayName("Lv.3: プランごとの送料を計算")
    void level3_calculation() {
        Shipping plan = Shipping.EXPRESS;
        int weight = 10; // 10kg

        // TODO: プランに応じた送料を計算してください
        int fee = switch (plan) {
            case STANDARD -> (weight * 100);
            case EXPRESS -> (weight * 200) + 500;
            case FREE -> 0;
        };

        // EXPRESSの計算: (10kg * 200円) + 500円 = 2500円
        assertEquals(2500, fee);
    }
}
