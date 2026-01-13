package com.sqlcanvas.todoapi.user;

import com.sqlcanvas.todoapi.user.domain.Address;
import com.sqlcanvas.todoapi.user.domain.Email;
import com.sqlcanvas.todoapi.user.domain.Rank;
import com.sqlcanvas.todoapi.user.domain.User;
import com.sqlcanvas.todoapi.user.repository.RankRepository;
import com.sqlcanvas.todoapi.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest // DBテスト用の軽量設定（H2などを自動起動）
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserCascadeTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RankRepository rankRepository;

    @Autowired
    private EntityManager entityManager; // キャッシュクリア用

    @BeforeEach
    void setup() {
        // FK制約があるので、先にマスタデータ（ランク）を入れておく
        // コンストラクタ引数: code, name, discountRate, requirementAmount
        Rank gold = new Rank("GOLD", "ゴールド会員", 10, 200000);
        rankRepository.save(gold);
    }

    @Test
    @DisplayName("Userを保存すると、持っているAddressも自動的に保存される（Cascade）")
    void cascade_persist_test() {
        // ---------------------------------------------------
        // 1. 準備: Userを作り、Addressを持たせる
        // ---------------------------------------------------
        Rank gold = rankRepository.findById("GOLD").orElseThrow();
        User user = new User("JPA太郎", new Email("jpa@example.com"), "password123", gold);

        Address tokyo = new Address("100-0001", "東京都", "千代田区", "1-1");
        Address osaka = new Address("530-0001", "大阪府", "大阪市", "2-2");

        // ドメインメソッドを使って追加！
        user.addAddress(tokyo);
        user.addAddress(osaka);

        // ---------------------------------------------------
        // 2. 実行: User「だけ」を保存する
        // ---------------------------------------------------
        userRepository.save(user);

        // ★ここが重要★
        // そのままだとメモリ上のキャッシュ(1次キャッシュ)から取得してしまうため、
        // ちゃんとSQLが発行されてDBに入ったか確認するために、強制的にキャッシュを消す。
        entityManager.flush(); // SQL発行
        entityManager.clear(); // キャッシュ削除

        // ---------------------------------------------------
        // 3. 検証: DBからUserを取り直して確認
        // ---------------------------------------------------
        User savedUser = userRepository.findById(user.getId()).orElseThrow();

        // Userは保存されているはず
        assertThat(savedUser.getName()).isEqualTo("JPA太郎");

        // 【ハイライト】Addressも一緒にDBから取れてくるはず！
        assertThat(savedUser.getAddresses()).hasSize(2);
        assertThat(savedUser.getAddresses().get(0).getCity()).isIn("千代田区", "大阪市");

        System.out.println("成功！UserのID: " + savedUser.getId());
        savedUser.getAddresses().forEach(a -> System.out.println("住所: " + a.getCity()));
    }
}