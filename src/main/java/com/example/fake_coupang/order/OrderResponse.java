
package com.example.fake_coupang.order;

import java.time.LocalDateTime;
import java.util.List;

// 주문 응답 정보
public record OrderResponse(
        Long id,
        Long userId,
        Long totalPrice,
        LocalDateTime orderedAt,
        Integer estimatedDeliveryHours,
        OrderStatus status,
        List<OrderItemResponse> items
) {
    // 주문 상품 상세 정보
    public record OrderItemResponse(
            Long id,
            Long productId,
            String productName,
            Integer orderCount,
            Long unitPrice
    ) {}
}
