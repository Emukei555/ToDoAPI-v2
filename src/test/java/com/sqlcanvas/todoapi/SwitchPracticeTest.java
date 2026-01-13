package com.sqlcanvas.todoapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SwitchPracticeTest {
    // 準備：成績を表すEnum
    enum Rank { S, A, B, C }

    // 準備：信号機を表すEnum
    enum Signal { RED, YELLOW, BLUE }

    @Test
    @DisplayName("Lv.1: ランクに応じたボーナスを計算する")
    public void level1_arrow_syntax() {
        Rank myRank = Rank.A;

        var bonus = switch (myRank) {
            case S -> 10000;
            case A -> 5000;
            case B -> 1000;
            case C -> 500;
        };
        assertEquals(5000, bonus);
    }
    @Test
    @DisplayName("Lv.2: 信号機のアクション判定")
    public void level2_signal_action() {
        Signal currentSignal = Signal.RED;

        // TODO: 信号の色に応じたアクションを返してください
        var action = switch (currentSignal) {
            // ヒント: 赤(RED) と 黄(YELLOW) は "STOP"
            case RED, YELLOW -> "STOP";
            // ヒント: 青(BLUE) は "GO"
            case BLUE -> "GO";
        };

        assertEquals("STOP", action);
    }

    @Test
    @DisplayName("Lv.3: 複数行の処理とyield")
    public void level3_yield() {
        Rank myRank = Rank.S;

        var item = switch (myRank) {
            // TODO: Sの場合はログを出してから "Gold Medal" を返す（yieldを使う）
            case S -> {
                System.out.println("Great!");
                yield  "Gold Medal"; // ←ここを埋める
            }

            // TODO: A, B, C の場合は "Participant" を返す（1行で書いてOK）
            // default を使っても良いし、 case A, B, C -> でも良い
            default -> "Participant";
        };

        assertEquals("Gold Medal", item);
    }

    @Test
    @DisplayName("Lv.4: パターンマッチングSwitch (Java 21)")
    public void level4_pattern_match() {
        Object obj = 100; // Integerを入れる

        // TODO: objの型によって分岐させてください
        String result = switch (obj) {
            // ヒント: case String s -> "String: " + s;
            // ヒント: case Integer i -> "Int: " + (i * 2);
            // ヒント: default -> "Unknown";
            case String s -> "String:" + s;
            case Integer i -> "Int: " + (i * 2);
                default -> "Unknown";
        };

        assertEquals("Int: 200", result);
    }
}

