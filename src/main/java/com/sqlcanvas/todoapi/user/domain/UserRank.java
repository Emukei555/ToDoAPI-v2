package com.sqlcanvas.todoapi.user.domain;

public enum UserRank {
    // 1. 定義と同時に値をセットする
    GOLD(0),      // ゴールドは手数料0円
    SILVER(300),  // シルバーは300円
    BRONZE(500);  // ブロンズは500円

    // 2. データを保持するフィールド（不変にするためfinal）
    private final int fee;

    // 3. コンストラクタ（ここで値を受け取る）
    UserRank(int fee) {
        this.fee = fee;
    }

    // 4. ロジック（手数料を返すメソッド）
    public int getFee() {
        return this.fee;
    }
}