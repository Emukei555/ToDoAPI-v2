package com.sqlcanvas.todoapi;

import org.junit.jupiter.api.Test;

// ★重要: extends AbstractIntegrationTest をつける！
// これがないと、ローカルのDBを見に行ってしまいエラーになります
class ToDoApiApplicationTests extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
        // Docker環境でアプリが起動するかチェック
    }

}