
package com.example.fake_coupang.orderItem;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // 주문 ID로 주문 상품 목록 조회
    List<OrderItem> findByOrder_Id(Long orderId);
}
