package com.sqlcanvas.todoapi.user.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DisplayName("Userドメインのテスト")
class UserTest {
    @Nested
    @DisplayName("会員ランクがゴールドの場合")
    class GoldRankContext {
        Rank goldRank = new Rank("GOLD", "ゴールド", 10, 0);
        private User goldUser;

        @Test
        @DisplayName("calculatePrice: 10%割引される")
        void calculatePrice_discounted() {
            // 準備：ランクだけゴールドにしたい（名前やメアドはどうでもいい）
            User goldUser = UserTestBuilder.aUser()
                    .withRank(goldRank)
                    .build();

            int result = goldUser.calculatePrice(10000);
            assertThat(result).isEqualTo(9000);
        }

        @Nested
        @DisplayName("会員ランクが通常の場合")
        class RegularRankContext {
            // 1. 通常ランクを作る
            Rank regularRank = new Rank("REGULAR", "通常", 0, 0);

            @Test
            @DisplayName("calculatePrice: 割引されない（定価のまま）")
            void calculatePrice_not_discounted() {
                // 2. Builderで「通常ランク」のユーザーを作る
                User regularUser = UserTestBuilder.aUser()
                        .withRank(regularRank) // ★ここをregularRankにする
                        .build();

                // 3. 10000円の商品は、10000円のままであるべき
                int result = regularUser.calculatePrice(10000);
                assertThat(result).isEqualTo(10000); // ★9000ではなく10000
            }
        }
    }
}