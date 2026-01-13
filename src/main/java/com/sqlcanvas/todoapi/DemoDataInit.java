package com.sqlcanvas.todoapi;

import com.sqlcanvas.todoapi.user.domain.Email;
import com.sqlcanvas.todoapi.user.domain.Password; // 追加
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
    private final UserCredentialsRepository userCredentialsRepository;
    private final RankRepository rankRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        // --- 1. ランクマスタの取得または作成 ---
        Rank gold = rankRepository.findByName("GOLD").orElseGet(() -> {
            Rank r = new Rank();
            r.setName("GOLD");
            r.setDisplayName("ゴールド会員");
            r.setDiscountRate(new BigDecimal("0.10"));
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

        // ★修正ポイント: 文字列ではなく Email オブジェクトを作る
        Email emailTaro = new Email("taro@example.com");

        // ★修正ポイント: リポジトリも Email オブジェクトで検索する
        Optional<UserCredentials> existingCredTaro = userCredentialsRepository.findByEmail(emailTaro);

        if (existingCredTaro.isEmpty()) {
            UserCredentials cred = new UserCredentials();
            // ★修正ポイント: Setterには Email オブジェクトを渡す
            cred.setEmail(emailTaro);

            // ★修正ポイント: Password オブジェクトでラップし、setPasswordを使う
            cred.setPassword(new Password(passwordEncoder.encode("password")));

            cred.setIsActive(true);

            User user1 = new User("テスト太郎", cred, gold);
            userRepository.save(user1);
            log.info("初期ユーザー(太郎)を登録しました: Rank=GOLD");
        }

        // 次郎も同様に修正
        Email emailJiro = new Email("jiro@example.com"); // StringからEmailへ
        Optional<UserCredentials> existingCredJiro = userCredentialsRepository.findByEmail(emailJiro);

        if (existingCredJiro.isEmpty()) {
            UserCredentials cred = new UserCredentials();
            cred.setEmail(emailJiro);
            cred.setPassword(new Password(passwordEncoder.encode("password")));
            cred.setIsActive(true);

            User user2 = new User("テスト次郎", cred, bronze);
            userRepository.save(user2);
            log.info("初期ユーザー(次郎)を登録しました: Rank=BRONZE");
        }

        log.info("初期データのチェック完了！");
    }
}