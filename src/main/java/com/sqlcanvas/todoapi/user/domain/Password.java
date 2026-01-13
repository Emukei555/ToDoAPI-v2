package com.sqlcanvas.todoapi.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Password implements Serializable {
    @Column(name = "password_hash", nullable = false)
    private String value;

    public Password(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("パスワードは必須です");
        }
        this.value = value;
    }
}
