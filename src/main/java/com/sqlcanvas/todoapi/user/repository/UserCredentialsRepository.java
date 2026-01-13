package com.sqlcanvas.todoapi.user.repository;

import com.sqlcanvas.todoapi.user.domain.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCredentialsRepository extends JpaRepository<UserCredentials, Long> {
    Optional<UserCredentials> findByEmail(String email);
}