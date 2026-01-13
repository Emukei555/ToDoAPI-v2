package com.sqlcanvas.todoapi.user.domain;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_addresses") // SQLで作ったテーブル名に合わせる
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA用
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DBに採番を任せる
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 親(User)へのFKは、User側で紐付けるのでここでは書かなくてもOK
    // もし双方向にするならここに @ManyToOne User user; が入るが、
    // まずは単方向（User -> Address）で考えるのがシンプル。

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Column(nullable = false)
    private String prefecture;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String street;

    // コンストラクタ（ここで生成時のルールを強制する！）
    // IDはDBが決めるので引数には含めない
    public Address(String postalCode, String prefecture, String city, String street) {
        if (postalCode == null || prefecture == null) {
            throw new IllegalArgumentException("住所の必須項目が足りません");
        }
        this.postalCode = postalCode;
        this.prefecture = prefecture;
        this.city = city;
        this.street = street;
    }

    // 値オブジェクト的な振る舞いとして「住所を変更する」のではなく
    // 「別の住所」として扱うため、Setterは作りません。
    // 修正したい場合は、このAddressを捨てて、新しいAddressをUserに追加します。
}