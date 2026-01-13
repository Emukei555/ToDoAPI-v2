package com.sqlcanvas.todoapi.book.domain.model;

import com.sqlcanvas.todoapi.book.dto.BookStock;
import lombok.Getter;

import java.util.UUID;

@Getter
public class Book {
    private final UUID id;
    private final String title;
    private BookStock stock;

    // 生成時のコンストラクタ
    public Book(String title, BookStock initialStock) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.stock = initialStock;
    }

    // ドメインロジック：貸出処理
    public void checkout() {
        this.stock = this.stock.decrease();
    }
}