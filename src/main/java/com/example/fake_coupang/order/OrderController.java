
package com.example.fake_coupang.order;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // 주문 생성 (POST /api/orders)
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    // 전체 주문 또는 사용자별 주문 조회 (GET /api/orders?userId=1)
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders(@RequestParam(required = false) Long userId) {

        if (userId != null) {
            return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
        }

        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // 주문 단건 조회 (GET /api/orders/{id})
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    // 주문 상태 변경 (PATCH /api/orders/{id}/status?status=SHIPPING)
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }
}
