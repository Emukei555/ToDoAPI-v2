package com.sqlcanvas.todoapi.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.regex.Pattern;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Email implements Serializable {

    // 簡易的なメールアドレス正規表現
    private static final String EMAIL_PATTERN = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";

    @Column(name = "email", nullable = false, unique = true)
    private String value;

    public Email(String value) {
        if (value == null || !Pattern.compile(EMAIL_PATTERN, Pattern.CASE_INSENSITIVE).matcher(value).matches()) {
            throw new IllegalArgumentException("不正なメールアドレス形式です: " + value);
        }
        this.value = value;
    }

    // 文字列として扱いたい時のために
    @Override
    public String toString() {
        return value;
    }
}