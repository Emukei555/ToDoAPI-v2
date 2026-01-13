package com.sqlcanvas.todoapi.auth;

import com.sqlcanvas.todoapi.user.domain.Email;
import com.sqlcanvas.todoapi.user.domain.User;
import com.sqlcanvas.todoapi.user.domain.UserCredentials;
import com.sqlcanvas.todoapi.user.repository.UserCredentialsRepository;
import com.sqlcanvas.todoapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserCredentialsRepository userCredentialsRepository;
    private final UserRepository userRepository;

    // ★ポイント: Overrideするメソッドの引数は必ず「String」でなければなりません
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String emailStr) throws UsernameNotFoundException {

        // 1. 入ってきた String を、ここで Email Value Object に変換します
        // バリデーションエラーならここで例外が出て安全に弾かれます
        Email email;
        try {
            email = new Email(emailStr);
        } catch (IllegalArgumentException e) {
            throw new UsernameNotFoundException("無効なメールアドレス形式です: " + emailStr);
        }

        // 2. Emailオブジェクトを使ってDB検索
        UserCredentials credentials = userCredentialsRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + emailStr));

        // 3. プロフィール情報を取得して返す
        User user = userRepository.findByCredentials_Id(credentials.getId())
                .orElseThrow(() -> new UsernameNotFoundException("プロフィール情報が見つかりません"));

        return new CustomUserDetails(user);
    }
}