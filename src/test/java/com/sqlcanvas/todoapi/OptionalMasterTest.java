package com.sqlcanvas.todoapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OptionalMasterTest {
    // 準備：重たい処理のシミュレーション
    private String getHeavyDefaultValue() {
        System.out.println("⚠️ 重たい処理が実行されました！");
        return "DEFAULT";
    }

    // 準備：ネストしたデータ構造
    record Profile(String bio) {}
    record Member(int id, String name, Optional<Profile> profile) {}
    // ↑ MemberはProfileを持っているかもしれないし、持っていないかもしれない

    @Test
    @DisplayName("Lv.1: orElse と orElseGet の決定的な違い")
    void level1_performance_trap() {
        Optional<String> opt = Optional.of("SUCCESS");

        System.out.println("--- orElse start ---");
        String resultA = opt.orElse(getHeavyDefaultValue());

        System.out.println("--- orElse start ---");
        String resultB = opt.orElseGet(() -> getHeavyDefaultValue());

        assertEquals("SUCCESS", resultA);
        assertEquals("SUCCESS", resultB);
    }

    @Test
    @DisplayName("Lv.2: 条件に合う時だけ値を取り出す")
    void level2_filter() {
        Optional<String> optName = Optional.of("Tanaka"); // 6文字

        // TODO: 名前が10文字以上ならその名前を、そうでなければ "Anonymous" を返す
        String result = optName
                // ヒント: Streamと同じ .filter(...) が使えます
                // .filter(n -> n.length() >= 10)
                .filter(n -> n.equals("Tanaka") && n.length() >= 10)
                .orElse("Anonymous");

        // Tanakaは10文字未満なので、Anonymousになるはず
        assertEquals("Anonymous", result);
    }

    @Test
    @DisplayName("Lv.3: 入れ子のOptionalを平坦化する")
    void level3_flatmap() {
        // Profileありのメンバー
        Profile profile = new Profile("I love Java.");
        Member member = new Member(1, "Sato", Optional.of(profile));

        // 検索結果として Optional<Member> が返ってきたと仮定
        Optional<Member> optMember = Optional.of(member);

        // TODO: ここから "I love Java." を取り出してください
        String bio = optMember
                // 1. Member -> Optional<Profile> を取り出す
                //    mapを使うと Optional<Optional<Profile>> になってしまう...
                //    ここで flatMap を使う！
                .flatMap(Member::profile)

                // 2. Profile -> String (bio) を取り出す
                //    Profileオブジェクトの中身(bio)はただのStringなので、ここは map でOK
                .map(Profile::bio)

                .orElse("No Bio");

        assertEquals("I love Java.", bio);
    }
}
