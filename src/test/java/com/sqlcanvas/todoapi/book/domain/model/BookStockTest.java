// test/domain/model/BookTest.java
package com.sqlcanvas.todoapi.book.domain.model;

import com.sqlcanvas.todoapi.book.dto.BookStock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class BookTest {

    @Test
    @DisplayName("在庫がある本は貸出ができる")
    void checkout_success() {
        // Arrange: 在庫1冊で「Effective Java」を作成
        Book book = new Book("Effective Java", new BookStock(1));

        // Act: 貸出実行
        book.checkout();

        // Assert: 在庫が0になっていること
        // Value Objectの中身までチェックして検証
        assertThat(book.getStock().value()).isEqualTo(0);
    }

    @Test
    @DisplayName("在庫がない本を借りようとするとエラーで弾かれる")
    void checkout_fail() {
        // Arrange: 在庫0冊の本
        Book book = new Book("Effective Java", new BookStock(0));

        // Act & Assert
        // BookStockが出す例外が、Book経由でもちゃんと出るか確認
        assertThatThrownBy(() -> book.checkout())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("在庫切れのため貸出できません");
    }
}