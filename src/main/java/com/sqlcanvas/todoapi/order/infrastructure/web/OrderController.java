package com.sqlcanvas.todoapi.order.infrastructure.web;

import com.sqlcanvas.todoapi.order.OrderMapper;
import com.sqlcanvas.todoapi.order.application.service.OrderService;
import com.sqlcanvas.todoapi.order.domain.model.Order;
import com.sqlcanvas.todoapi.order.domain.repository.OrderRepository;
import com.sqlcanvas.todoapi.order.infrastructure.dto.OrderRequest;
import com.sqlcanvas.todoapi.order.infrastructure.dto.OrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@RequestBody @Valid OrderRequest request) {

        // 1. 処理
        Order order = orderService.createOrder(request);

        // 2. 変換 (Mapperに丸投げ！)
        return orderMapper.toResponse(order);
    }

    @GetMapping
    public List<OrderResponse> getAll() {
        // service.findAll() を呼んで、mapperで変換して返すだけ
        // (serviceに findAll メソッドがない場合は作ってください)
        List<Order> orders = orderRepository.findAll();
        return orders.stream().map(orderMapper::toResponse).toList();
    }
}
