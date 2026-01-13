package com.sqlcanvas.todoapi.book.domain.model;

import com.sqlcanvas.todoapi.book.dto.BookStock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BorrowedBooksTest {

    @Test
    @DisplayName("nullのリストで作成しようとすると即死すること")
    void constructor_validation_null() {
        assertThatThrownBy(() -> new BorrowedBooks(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("nullが含まれるリストで作成しようとすると即死すること")
    void constructor_validation_contains_null() {
        List<Book> listWithNull = Collections.singletonList(null);

        // List.copyOfが仕事をするか確認
        assertThatThrownBy(() -> new BorrowedBooks(listWithNull))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("5冊持っている状態で追加するとエラーになること")
    void add_limit_error() {
        // Arrange: 限界まで詰め込む
        // (テスト用にダミーの本を作るのが面倒なら、Mockを使ってもいいですが、
        //  ValueObjectならnewしまくるのが一番早いです)
        BorrowedBooks books = new BorrowedBooks();
        for (int i = 0; i < 5; i++) {
            books = books.add(new Book("Book " + i, new BookStock(1)));
        }

        // Act & Assert
        BorrowedBooks finalBooks = books;
        assertThatThrownBy(() -> finalBooks.add(new Book("Outlaw", new BookStock(1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("上限5冊");
    }
}