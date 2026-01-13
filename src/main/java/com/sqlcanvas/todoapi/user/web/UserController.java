package com.sqlcanvas.todoapi.user.web;

import com.sqlcanvas.todoapi.user.domain.User;
import com.sqlcanvas.todoapi.user.infrastructure.dto.UserResponse;
import com.sqlcanvas.todoapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/users/{id}")
    public UserResponse getUser(@PathVariable Long id) { // 戻り値を変更
        // 1. ドメインモデルを取得 (ここではEntity)
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません"));

        // 2. DTOに変換して返す (パスワードなどはここで削ぎ落とされる)
        return UserResponse.from(user);
    }

    // 例: GET /users/1/price-check?amount=10000
    // ユーザーID:1 の人が 10,000円の商品を買ったら、いくらになるか計算する
    @GetMapping("/users/{id}/price-check")
    public String checkPrice(@PathVariable Long id, @RequestParam int amount) {
        User user = userRepository.findById(id).orElseThrow();

        // ★ここでリッチドメインモデルのメソッドを使う！
        // Serviceにロジックを書かず、User自身に計算させているのがポイント
        int discountedPrice = user.calculatePrice(amount);

        return String.format(
                "%s さんのランクは %s です。\n定価: %d円 -> 割引後: %d円",
                user.getName(),
                user.getRankEntity().getName(),
                amount,
                discountedPrice
        );
    }
}
