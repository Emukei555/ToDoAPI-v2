package com.sqlcanvas.todoapi;

// src/test/java/com/sqlcanvas/todoapi/AbstractIntegrationTest.java

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers // Testcontainersを有効化
public abstract class AbstractIntegrationTest {

    // ★魔法のコード
    // DockerでPostgreSQL(latest)を立ち上げる
    @Container
    @ServiceConnection // これをつけると、接続設定(URL, User, Pass)を勝手にSpringに繋いでくれる！
    protected static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

}