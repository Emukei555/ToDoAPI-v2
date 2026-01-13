package com.sqlcanvas.todoapi.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- リレーション: 認証情報 (1対1) ---
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "credential_id", nullable = false, unique = true)
    private UserCredentials credentials;

    // --- 基本情報 ---
    @Column(nullable = false)
    private String name;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "date_of_birth")
    private java.time.LocalDate dateOfBirth;

    // --- リレーション: 住所 (1対多) ---
    // Userが親、Addressが子。Userを消したらAddressも消える設定(CascadeType.ALL)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rank_id", nullable = false)
    private Rank rankEntity;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- コンストラクタ ---
    // JPA用のデフォルトコンストラクタ
    protected User() {}

    // ドメイン用のコンストラクタ
    // User作成時は必ずCredentialsも必要なため、引数で受け取る
    public User(String name, UserCredentials credentials, Rank rankEntity) {
        if (name == null || credentials == null) {
            throw new IllegalArgumentException("名前と認証情報は必須です");
        }
        if (rankEntity == null) {
            throw new IllegalArgumentException("ランク指定は必須です");
        }
        this.rankEntity = rankEntity;
        this.name = name;
        this.credentials = credentials;
        this.rankEntity = rankEntity;
        // 双方向リレーションの整合性を保つため、Credentials側にはセットしない（CredentialsにはUserフィールドがないため）
    }

    // --- ドメインロジック ---

    /**
     * 割引後の価格を計算する
     * (データだけでなく計算ロジックを持つリッチなメソッド)
     */

    public int calculatePrice(int originalPrice) {
        return originalPrice - this.rankEntity.calculateDiscountAmount(originalPrice);
    }

    // Rankエンティティを変更する場合のメソッド
    public void changeRankEntity(Rank newRankEntity) {
        if (newRankEntity == null) {
            throw new IllegalArgumentException("ランクエンティティは必須です");
        }
        this.rankEntity = newRankEntity;
    }

    // 住所追加ロジック（ビジネスルール: 上限3つ）
    public void addAddress(Address newAddress) {
        if (newAddress == null) {
            throw new IllegalArgumentException("アドレスは必須です");
        }
        if (this.addresses.size() >= 3) {
            throw new IllegalStateException("登録できる住所は3つまでです");
        }

        // 重要: JPAの双方向リレーションの場合、子側にも親をセットする必要がある
        // Addressクラスに setUser(User user) が必要
        // newAddress.setUser(this);

        this.addresses.add(newAddress);
    }

    public List<Address> getAddresses() {
        return Collections.unmodifiableList(this.addresses);
    }
}

