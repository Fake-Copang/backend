
package com.example.fake_coupang.order;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 사용자 ID로 주문 목록 조회
    List<Order> findByUserId(Long userId);
}
