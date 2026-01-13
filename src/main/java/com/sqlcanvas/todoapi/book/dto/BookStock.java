package com.sqlcanvas.todoapi.book.dto;

// Java 21: recordで不変(Immutable)な値オブジェクトを作る
public record BookStock(int value) {
    public BookStock {
        if (value < 0) {
            throw new IllegalArgumentException("在庫数は0以上である必要があります");
        }
    }

    // 在庫があるかどうかチェックするロジック
    public boolean isAvailable() {
        return value > 0;
    }

    // 貸出：在庫を1つ減らした新しい在庫オブジェクトを返す
    public BookStock decrease() {
        if (!isAvailable()) {
            throw new IllegalStateException("在庫切れのため貸出できません");
        }
        return new BookStock(this.value - 1);
    }
}
