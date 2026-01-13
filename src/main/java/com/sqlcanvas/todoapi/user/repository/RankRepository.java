package com.sqlcanvas.todoapi.user.repository;

import com.sqlcanvas.todoapi.user.domain.Rank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RankRepository extends JpaRepository<Rank, String> {
    Optional<Rank> findByName(String name);
}