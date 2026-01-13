package com.sqlcanvas.todoapi.auth;

import com.sqlcanvas.todoapi.user.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    // 変更点1: フィールドの型を UserEntity から User に変更
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public Long getId() {
        return user.getId();
    }


    @Override
    public String getUsername() {
        return user.getCredentials().getEmail().getValue();
    }

    @Override
    public String getPassword() {
        return user.getCredentials().getPassword().getValue();
    }

    // --- 以下、UserDetailsの必須メソッド ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}