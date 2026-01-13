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
    private final UserRepository userRepository; // User情報も必要なら残す

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // 1. まず認証テーブル(UserCredentials)からメールで検索
        UserCredentials credentials = userCredentialsRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + email));

        // 2. Userテーブルの情報が必要なら、CredentialsからUserを取得できる
        // (UserCredentials側にUserへの参照がない場合は、UserRepositoryで検索する)
        // 今回の設計では User -> Credentials という方向なので、
        // 逆に Credentials から User を探すには UserRepository で検索が必要かもしれません。

        // もし UserCredentials 側に @OneToOne(mappedBy = "credentials") User user; があれば
        // credentials.getUser() で取れますが、なければ以下のように探します。
        User user = userRepository.findByCredentials_Id(credentials.getId())
                .orElseThrow(() -> new UsernameNotFoundException("プロフィール情報が見つかりません"));

        // 3. CustomUserDetails を返す
        return new CustomUserDetails(user);
    }
}