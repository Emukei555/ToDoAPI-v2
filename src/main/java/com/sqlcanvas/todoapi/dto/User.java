package com.sqlcanvas.todoapi.dto;

import java.util.Optional;

public record User (int id, String name, String nickname){
    private Optional<User> findById(int id) {
        if (id == 1) return Optional.of(new User(1, "Tanaka", "Tana-chan"));
        if (id == 2) return Optional.of(new User(2, "Suzuki", null)); // あだ名なし
        return Optional.empty(); // ユーザーなし
    }
}

