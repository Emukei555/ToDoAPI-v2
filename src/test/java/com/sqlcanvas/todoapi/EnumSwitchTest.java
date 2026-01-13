package com.sqlcanvas.todoapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnumSwitchTest {
    // 決済方法
    enum PaymentMethod {
        CASH,       // 現金
        CREDIT,     // クレジットカード
        QR_CODE,    // QR決済
        BANK_TRANSFER // 銀行振込
    }

    @Test
    @DisplayName("Lv.1: 決済方法ごとの手数料を計算")
    void level1_fee_calc() {
        PaymentMethod method = PaymentMethod.BANK_TRANSFER;

        // 手数料: 現金0円, クレカ0円, QR0円, 銀行振込330円
        int fee = switch (method) {
            case CASH -> 0;
            case CREDIT -> 0;
            case QR_CODE -> 0;
            case BANK_TRANSFER -> 330;
            // default は書かない！書くと「新しい決済」が増えた時に気づけないから。
        };

        assertEquals(330, fee);
    }

    @Test
    @DisplayName("Lv.2: ポイント還元率の判定")
    void level2_grouping() {
        PaymentMethod method = PaymentMethod.QR_CODE;

        // TODO: ポイント還元率（%）を返してください
        // 現金と銀行振込は 0%
        // クレジットとQRは 1%
        int pointRate = switch (method) {
            // ヒント: case CASH, _______ -> 0;
            case CASH, BANK_TRANSFER -> 0;

            // ヒント: 残りのキャッシュレス決済をまとめる
            case CREDIT, QR_CODE -> 1;
        };

        assertEquals(1, pointRate);
    }

    @Test
    @DisplayName("Lv.3: 支払いに失敗した時の「次の手段」を提案する")
    void level3_next_method() {
        PaymentMethod current = PaymentMethod.CREDIT;

        // TODO: 上記の要件に従って、次の支払い方法(PaymentMethod)を返してください
        // 戻り値は PaymentMethod 型、一部 null を返すので var ではなく型を書くか、
        // PaymentMethod next = switch... としてください。

        PaymentMethod next = switch (current) {
            // ここにロジックを実装
            case CASH -> PaymentMethod.CREDIT;
            case CREDIT -> PaymentMethod.QR_CODE;
            case QR_CODE -> PaymentMethod.BANK_TRANSFER;
            case BANK_TRANSFER -> null;
        };
        assertEquals(PaymentMethod.QR_CODE, next);
    }
}
