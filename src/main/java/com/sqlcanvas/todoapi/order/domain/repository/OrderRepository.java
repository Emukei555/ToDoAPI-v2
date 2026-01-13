package com.sqlcanvas.todoapi.order.domain.repository;

import com.sqlcanvas.todoapi.order.OrderStatus;
import com.sqlcanvas.todoapi.order.domain.model.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // ユーザー情報(User) と 注文明細(Items) を
    // たった1回のSQLで全部持ってくる最強メソッド
    @Query("""
                SELECT DISTINCT o
                FROM Order o
                JOIN FETCH o.user
                JOIN FETCH o.items
            """)
    List<Order> findAllWithUserAndItems();


    // 対策1: @EntityGraph (一番簡単)
    // 注文を取るときに、一緒に(JOINして) items も user も取ってこい！という命令
    @EntityGraph(attributePaths = {"items", "user", "items.product"})
    List<Order> findAll();

    // 対策2: JPQL (自分でSQLっぽく書く)
    // 複雑な条件のときはこっち
    @Query("SELECT o FROM Order o JOIN FETCH o.items JOIN FETCH o.user WHERE o.status = :status")
    List<Order> findByStatusWithItems(@Param("status") OrderStatus status);
}