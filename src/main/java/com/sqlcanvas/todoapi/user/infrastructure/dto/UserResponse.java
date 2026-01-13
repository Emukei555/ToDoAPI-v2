package com.sqlcanvas.todoapi.user.infrastructure.dto;

import com.sqlcanvas.todoapi.user.domain.User;

public record UserResponse(
        Long id,
        String name,
        com.sqlcanvas.todoapi.user.domain.Email email,      // Value ObjectではなくStringにする
        String rankName,   // Rankオブジェクトではなく、ランク名だけ返す
        java.math.BigDecimal discountRate   // 割引率も便利なので返してあげる
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getCredentials().getEmail(), // EmailオブジェクトをStringに変換
                user.getRankEntity().getName(),   // Rankから名前だけ抽出
                user.getRankEntity().getDiscountRate()
        );
    }
}
