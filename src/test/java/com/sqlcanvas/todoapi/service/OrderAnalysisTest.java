package com.sqlcanvas.todoapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for analyzing orders using Java streams and other modern features.
 * This class contains unit tests for various order-related operations.
 */
class OrderAnalysisTest {
    /**
     * Enum representing the status of an order.
     */
    enum OrderStatus {
        PENDING,
        SHIPPED,
        DELIVERED,
        CANCELED
    }

    /**
     * Enum representing the payment methods available for orders.
     */
    enum PaymentMethod {
        CREDIT_CARD,
        PAYPAL,
        BANK_TRANSFER
    }

    /**
     * Record representing an order with its details.
     *
     * @param id            The unique identifier of the order.
     * @param customerName  The name of the customer who placed the order.
     * @param amount        The total amount of the order.
     * @param status        The current status of the order.
     * @param paymentMethod The payment method used for the order.
     */
    record Order(
            int id,
            String customerName,
            long amount, // 金額
            OrderStatus status,
            PaymentMethod paymentMethod
    ) {}

    /**
     * List of orders used for testing. This is initialized in the setup method.
     */
    private List<Order> orders;

    /**
     * Sets up the test data before each test method is executed.
     * Initializes an immutable list of sample orders.
     */
    @BeforeEach
    void setUp() {
        orders = List.of(
                new Order(1, "Alice", 5000, OrderStatus.DELIVERED, PaymentMethod.CREDIT_CARD),
                new Order(2, "Bob", 3000, OrderStatus.PENDING, PaymentMethod.PAYPAL),
                new Order(3, "Charlie", 12000, OrderStatus.SHIPPED, PaymentMethod.CREDIT_CARD),
                new Order(4, "David", 8000, OrderStatus.CANCELED, PaymentMethod.BANK_TRANSFER),
                new Order(5, "Eve", 15000, OrderStatus.DELIVERED, PaymentMethod.PAYPAL),
                new Order(6, "Frank", 2000, OrderStatus.PENDING, PaymentMethod.CREDIT_CARD)
        );
    }

    /**
     * Tests the calculation of total revenue from delivered orders.
     * Verifies that the sum of amounts for delivered orders is correct.
     */
    @Test
    void calculateTotalRevenueOfDeliveredOrders() {
        var totalAmount = orders.stream()
                .filter(order -> order.status() == OrderStatus.DELIVERED)
                .mapToLong(Order::amount)
                .sum();

        assertEquals(20000, totalAmount);
    }

    /**
     * Tests retrieval of customer names who used credit card as payment method.
     * Verifies the count and specific names in the result list.
     */
    @Test
    void getCreditCardUserNames() {
        List<String> customerNames = orders.stream()
                .filter(order -> order.paymentMethod() == PaymentMethod.CREDIT_CARD)
                .map(Order::customerName)
                .toList();

        assertEquals(3, customerNames.size());
        assertTrue(customerNames.containsAll(List.of("Alice", "Charlie", "Frank")));
    }

    /**
     * Tests grouping of orders by their status.
     * Verifies the map is not null and checks sizes for specific statuses.
     */
    @Test
    void groupOrdersByStatus() {
        Map<OrderStatus, List<Order>> ordersByStatus = orders.stream()
                .collect(Collectors.groupingBy(Order::status));

        assertNotNull(ordersByStatus);
        assertEquals(2, ordersByStatus.get(OrderStatus.PENDING).size());
        assertEquals(1, ordersByStatus.get(OrderStatus.CANCELED).size());
    }

    /**
     * Tests finding the most expensive order.
     * Verifies the customer name and amount of the most expensive order.
     */
    @Test
    void findMostExpensiveOrder() {
        Order mostExpensive = orders.stream()
                .max(Comparator.comparingLong(Order::amount)) // comparingLongでもOK
                .orElseThrow(NoSuchElementException::new);

        assertEquals("Eve", mostExpensive.customerName());
        assertEquals(15000, mostExpensive.amount());
    }

    /**
     * Tests generation of status message using switch expression.
     * Verifies the message for a specific order status.
     */
    @Test
    void getStatusMessageUsingSwitch() {
        var targetOrder = orders.getFirst();
        String message = switch (targetOrder.status()) {
            case PENDING   -> "Preparing for shipment";
            case SHIPPED   -> "Shipped";
            case DELIVERED -> "Delivery completed";
            case CANCELED  -> "Cancelled";
        };

        assertEquals("Delivery completed", message);
    }
}