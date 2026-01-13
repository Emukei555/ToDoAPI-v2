package com.sqlcanvas.todoapi.auth;

import com.sqlcanvas.todoapi.auth.infrastructure.web.dto.JwtResponse;
import com.sqlcanvas.todoapi.auth.infrastructure.web.dto.LoginRequest;
import com.sqlcanvas.todoapi.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public JwtResponse login(@RequestBody LoginRequest request) {

        // 1. ID/パスワードで認証を行う
        // (失敗するとここで例外が出て、401エラーなどが返る)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // 2. 認証情報をセキュリティコンテキストに保存
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. 認証成功！トークンを発行する
        // (Authenticationからユーザー情報を取り出す)
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String jwt = tokenProvider.generateToken(userDetails);

        // 4. トークンをクライアントに返す
        return new JwtResponse(jwt);
    }
}