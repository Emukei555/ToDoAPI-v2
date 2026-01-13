package com.sqlcanvas.todoapi.user.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @ParameterizedTest
    @NullSource // 1. nullの場合
    @ValueSource(strings = {
            "",              // 2. 空文字
            "invalid-email", // 3. @がない
            "user@",         // 4. @の後ろがない
            "@example.com",  // 5. @の前がない
            "user name@example.com" // 6. 空白がある
    })
    @DisplayName("不正な値の場合、生成時に例外がスローされる")
    void invalid_format_test(String invalidEmail) {

        // 生成しようとして...
        assertThatThrownBy(() -> {
            new Email(invalidEmail);
        })
                // その結果、IllegalArgumentException が起きることを期待する
                .isInstanceOf(IllegalArgumentException.class);

        // メッセージの内容まで検証したい場合はこう繋げる
        // .hasMessageContaining("メールアドレス");
    }
}