package com.sqlcanvas.todoapi.user.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data // ★このアノテーションがないと getEmail() も getPassword() も使えません
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Embedded
    private Email email;

    // ▼ さっき追加したパスワード
    private String password;

    @Enumerated(EnumType.STRING) // ★DBには "GOLD" という文字で保存される
    @Column(nullable = false)
    private UserRank rank;

    public int getCurrentFee() {
        // 自分(UserEntity)が持っている rank(UserRank) に「いくら？」と聞く
        return this.rank.getFee();
    }

    // もしランクを変更するメソッドを作りたいならこう
    public void promoteToGold() {
        if (this.rank == UserRank.GOLD) {
            throw new IllegalStateException("すでにゴールド会員です");
        }
        this.rank = UserRank.GOLD;
    }
}