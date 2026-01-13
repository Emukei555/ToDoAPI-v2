package com.sqlcanvas.todoapi.book.domain.model;

import java.util.List;
import java.util.Objects;

public class BorrowedBooks {

    private static final int MAX_LIMIT = 5;
    private final List<Book> list;

    // 1. 空のリストで初期化（これは安全なのでチェック不要）
    public BorrowedBooks() {
        this.list = List.of();
    }

    /**
     * 2. メインのコンストラクタ（ここが最強の防波堤）
     * 外部からリストを渡される場合、ここで全ての不正を弾く。
     */
    public BorrowedBooks(List<Book> list) {
        // バリデーション①: 引数そのものがnullでないか
        Objects.requireNonNull(list, "リストはnullであってはいけません");

        // バリデーション②: リストの上限を超えていないか（DBからの復元時などの整合性チェック）
        if (list.size() > MAX_LIMIT) {
            throw new IllegalArgumentException("貸出上限(" + MAX_LIMIT + "冊)を超えた状態では生成できません");
        }

        // バリデーション③: リストの中にnullが含まれていないか & 不変リスト化
        // List.copyOf は Java 10以降の機能。
        // null要素があるとNullPointerExceptionを出し、かつ変更不可能なリストを作ってくれる神メソッド。
        this.list = List.copyOf(list);
    }

    /**
     * 3. 本を追加するメソッド
     */
    public BorrowedBooks add(Book newBook) {
        // バリデーション④: 追加する本がnullでないか
        Objects.requireNonNull(newBook, "追加する本はnullであってはいけません");

        // バリデーション⑤: 冊数制限チェック (ビジネスルール)
        if (this.list.size() >= MAX_LIMIT) {
            throw new IllegalStateException("これ以上借りられません（上限" + MAX_LIMIT + "冊）");
        }

        // バリデーション⑥: 重複チェック (ビジネスルール)
        if (this.list.contains(newBook)) {
            throw new IllegalArgumentException("同じ本は2冊借りられません");
        }

        // 全てクリアしたら、新しいリストを作って返す（不変性）
        List<Book> newList = java.util.stream.Stream.concat(
                this.list.stream(),
                java.util.stream.Stream.of(newBook)
        ).toList();

        return new BorrowedBooks(newList);
    }

    // 参照用
    public List<Book> asList() {
        return this.list;
    }
}