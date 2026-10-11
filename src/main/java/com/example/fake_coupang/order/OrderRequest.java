
package com.example.fake_coupang.order;

import java.util.List;

// 주문 생성 요청 정보
public record OrderRequest(
        Long userId,
        List<OrderItemRequest> items
) {
    // 주문할 개별 상품 정보
    public record OrderItemRequest(
            Long productId,
            Integer orderCount
    ) {}
}
