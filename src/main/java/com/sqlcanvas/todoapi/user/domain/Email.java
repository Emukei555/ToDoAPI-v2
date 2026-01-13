package com.sqlcanvas.todoapi.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.regex.Pattern;

@Embeddable // ★JPA: このクラスは他のEntity(User)に埋め込まれます
@Getter
@EqualsAndHashCode // ★VOの命: 値が同じなら「同じもの」とみなす
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA用
public class Email implements Serializable {

    // 簡易的なメール形式チェック用正規表現
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final Pattern PATTERN = Pattern.compile(EMAIL_PATTERN);

    @Column(name = "email", nullable = false, unique = true) // DBのカラム定義をここに移動
    private String value;

    // ★コンストラクタで「不正な値」を門前払いする
    public Email(String value) {
        if (value == null) {
            throw new IllegalArgumentException("メールアドレスは必須です");
        }
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("メールアドレスの形式が正しくありません: " + value);
        }
        this.value = value;
    }

    // 値をStringで取り出したい時用（Getterがあるので getValue() でもいいが、toStringも便利）
    @Override
    public String toString() {
        return this.value;
    }
}