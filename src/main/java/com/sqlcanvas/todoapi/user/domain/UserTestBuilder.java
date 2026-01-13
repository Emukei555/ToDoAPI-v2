package com.sqlcanvas.todoapi.user.domain;

import java.math.BigDecimal;

public class UserTestBuilder {

    // 1. デフォルト値を決めておく（ここが超重要！）
    // これのおかげで、テストで設定しない項目は勝手にこれらが使われる
    private String name = "デフォルト太郎";
    private Email email = new Email("default@example.com");
    private String password = "defaultPassword";
    private Rank rank = new Rank(1L, "REGULAR", "通常", BigDecimal.ZERO);

    // 2. スタティックファクトリメソッド（開始の合図）
    public static UserTestBuilder aUser() {
        return new UserTestBuilder();
    }

    // 3. カスタマイズ用メソッド（自分自身を返すのがコツ）
    public UserTestBuilder withName(String name) {
        this.name = name;
        return this; // チェーンできるように自分を返す
    }

    public UserTestBuilder withEmail(String emailStr) {
        this.email = new Email(emailStr);
        return this;
    }

    public UserTestBuilder withRank(Rank rank) {
        this.rank = rank;
        return this;
    }

    // パスワードはテストであまり変えないから、メソッド作らなくてもいい（必要になったら足す）

    // 4. 最後にドメインオブジェクトを生成する
    public User build() {
        // 1. まずRankを用意（既存コードのままでOK、上の@AllArgsConstructorで直る）
        // private Rank rank = new Rank("REGULAR", "通常", new BigDecimal("0"), ...);

        // 2. 認証情報(Credentials)を作成
        UserCredentials credentials = new UserCredentials();
        credentials.setEmail(this.email.getValue()); // Email型を使っている場合
        credentials.setPasswordHash(this.password);
        credentials.setIsActive(true);

        // 3. 新しいUserコンストラクタを使用
        // User(String name, UserCredentials credentials, Rank rank)
        return new User(this.name, credentials, this.rank);
    }
}