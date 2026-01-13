package com.sqlcanvas.todoapi.user.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor; // 引数なしコンストラクタ用
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "user_ranks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Rank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // BRONZE, SILVER...

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "discount_rate", nullable = false)
    private BigDecimal discountRate;

    // ビジネスロジック: 割引「額」を計算する
    // 引数が int なので戻り値も int (切り捨て) としています
    public int calculateDiscountAmount(int originalPrice) {; // 端数切り捨て
        if (this.discountRate == null || this.discountRate.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        // 価格 * 割引率
        BigDecimal priceBD = new BigDecimal(originalPrice);
        BigDecimal discountAmount = priceBD.multiply(this.discountRate);

        return discountAmount.intValue(); // 端数切り捨て
    }
}