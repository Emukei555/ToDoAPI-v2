package com.sqlcanvas.todoapi.order;

import com.sqlcanvas.todoapi.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

// さっき作った親クラスを継承するだけ！
class OrderRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void DockerのPostgresSQLに接続できているか確認() throws Exception {
        // 1. コンテナが起動しているか確認
        assertThat(postgres.isRunning()).isTrue();

        // 2. 実際にDBに接続してSQLが打てるか確認
        try (Connection conn = dataSource.getConnection()) {
            assertThat(conn.isValid(1)).isTrue();

            // カタログ名（DB名）が "test" (Testcontainersのデフォルト) になっているはず
            System.out.println("接続中のDB: " + conn.getCatalog());
        }
    }
}