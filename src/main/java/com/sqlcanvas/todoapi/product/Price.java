package com.sqlcanvas.todoapi.product;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPAのために空のコンストラクタが必要
@EqualsAndHashCode // 値が同じなら「同じ価格」とみなす
@ToString
public class Price {

    // DBに合わせて Long に変更推奨（intだと桁あふれのリスクがあるため）
    @Column(name = "price", nullable = false)
    private Long value;

    public Price(Long value) {
        if (value == null || value < 0) {
            throw new IllegalArgumentException("価格は0以上である必要があります。入力値: " + value);
        }
        this.value = value;
    }

    // 使いやすくするためのオーバーロード（longプリミティブ用）
    public Price(long value) {
        this(Long.valueOf(value));
    }
}