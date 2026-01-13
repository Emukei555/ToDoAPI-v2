package com.sqlcanvas.todoapi.order.application.service;

import com.sqlcanvas.todoapi.order.domain.model.Order;
import com.sqlcanvas.todoapi.order.domain.model.OrderItem;
import com.sqlcanvas.todoapi.order.domain.repository.OrderRepository;
import com.sqlcanvas.todoapi.order.infrastructure.dto.OrderItemRequest;
import com.sqlcanvas.todoapi.order.infrastructure.dto.OrderRequest;
import com.sqlcanvas.todoapi.product.Product;
import com.sqlcanvas.todoapi.product.ProductRepository;
import com.sqlcanvas.todoapi.shared.domain.exception.ResourceNotFoundException;
import com.sqlcanvas.todoapi.user.domain.User;
import com.sqlcanvas.todoapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /**
     * 注文を作成するメインロジック
     */
    @Transactional // ★超重要: 途中でエラーが出たら全部キャンセル(ロールバック)する
    public Order createOrder(OrderRequest request) {

        // 1. ユーザーが存在するか確認
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません ID: " + request.userId()));

        // 2. 注文の「箱」を作る
        Order order = new Order(user);

        long totalAmount = 0;

        for (OrderItemRequest itemReq : request.items()) {
            // 1. 商品を取得
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            // 2. 商品に「在庫減らして」と命令 (ロジックはProductの中)
            product.decreaseStock(itemReq.quantity());

            // 3. 明細を作成 (価格スナップショットはOrderItemの中)
            OrderItem item = OrderItem.create(product, itemReq.quantity());

            // 4. 注文に追加 (合計金額計算はOrderの中)
            order.addItem(item);
        }

        // 4. 保存（CascadeType.ALLのおかげで、order_itemsも自動でINSERTされる）
        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();

        order.cancel();

        // 2. 在庫を戻す処理
        for (OrderItem item : order.getItems()) {
            item.getProduct().increaseStock(item.getQuantity());
        }

        return orderRepository.save(order);
    }
}