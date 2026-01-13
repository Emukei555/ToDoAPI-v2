package com.sqlcanvas.todoapi.user.repository;

import com.sqlcanvas.todoapi.user.domain.Email;
import com.sqlcanvas.todoapi.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// ★ここを <User, Long> にする
public interface UserRepository extends JpaRepository<User, Long> {
    // コメントアウト
    // Optional<User> findByEmail(Email email);

    // もし「メールアドレスからユーザー情報を検索したい」場合はこう書きます（今回は不要かも）
    // Optional<User> findByCredentials_Email(String email);
    Optional<User> findByCredentials_Id(Long credentialId);
}