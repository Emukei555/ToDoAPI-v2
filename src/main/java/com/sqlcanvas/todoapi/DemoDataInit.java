package com.sqlcanvas.todoapi;

import com.sqlcanvas.todoapi.user.domain.Rank;
import com.sqlcanvas.todoapi.user.domain.User;
import com.sqlcanvas.todoapi.user.domain.UserCredentials;
import com.sqlcanvas.todoapi.user.repository.RankRepository;
import com.sqlcanvas.todoapi.user.repository.UserCredentialsRepository;
import com.sqlcanvas.todoapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DemoDataInit implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserCredentialsRepository userCredentialsRepository; // 追加
    private final RankRepository rankRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional // 複数のテーブルを更新するのでトランザクションをかける
    public void run(String... args) throws Exception {

        // --- 1. ランクマスタの取得または作成 ---
        // ID(数値)ではなく、name(文字列)で検索します
        Rank gold = rankRepository.findByName("GOLD").orElseGet(() -> {
            Rank r = new Rank();
            r.setName("GOLD");
            r.setDisplayName("ゴールド会員");
            r.setDiscountRate(new BigDecimal("0.10")); // 10%
            // r.setDescription("..."); // 必要ならセット
            return rankRepository.save(r);
        });

        Rank bronze = rankRepository.findByName("BRONZE").orElseGet(() -> {
            Rank r = new Rank();
            r.setName("BRONZE");
            r.setDisplayName("ブロンズ会員");
            r.setDiscountRate(BigDecimal.ZERO);
            return rankRepository.save(r);
        });

        // --- 2. ユーザーの登録 ---
        // Emailの重複チェックは UserCredentials テーブルに対して行う
        String emailTaro = "taro@example.com";
        Optional<UserCredentials> existingCredTaro = userCredentialsRepository.findByEmail(emailTaro);

        if (existingCredTaro.isEmpty()) {
            // Step A: まず認証情報を作る
            UserCredentials cred = new UserCredentials();
            cred.setEmail(emailTaro);
            cred.setPasswordHash(passwordEncoder.encode("password"));
            cred.setIsActive(true);
            // ※ここで cred を save() するかは CascadeType.ALL の設定次第ですが、
            //   Userと一緒に保存される設定ならここでは save 不要です。

            // Step B: Userを作る (CredentialsとRankを渡す)
            // コンストラクタ: User(String name, UserCredentials credentials, Rank rankEntity)
            User user1 = new User("テスト太郎", cred, gold);

            // ★古い Enum の setRank は削除します（Rankエンティティがあれば十分）

            userRepository.save(user1);
            log.info("初期ユーザー(太郎)を登録しました: Rank=GOLD");
        }

        String emailJiro = "jiro@example.com";
        Optional<UserCredentials> existingCredJiro = userCredentialsRepository.findByEmail(emailJiro);

        if (existingCredJiro.isEmpty()) {
            UserCredentials cred = new UserCredentials();
            cred.setEmail(emailJiro);
            cred.setPasswordHash(passwordEncoder.encode("password"));
            cred.setIsActive(true);

            User user2 = new User("テスト次郎", cred, bronze);

            userRepository.save(user2);
            log.info("初期ユーザー(次郎)を登録しました: Rank=BRONZE");
        }

        log.info("初期データのチェック完了！");
    }
}