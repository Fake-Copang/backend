
package com.example.fake_coupang.orderItem;

import com.example.fake_coupang.order.OrderStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository
        extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder_Id(Long orderId);

    @Query("""
            SELECT oi
            FROM OrderItem oi
            JOIN FETCH oi.order o
            JOIN FETCH oi.product p
            WHERE o.user.id = :userId
            AND o.status = :status
            ORDER BY oi.id ASC
        """)
    List<OrderItem> findByUserIdAndOrderStatus(
            @Param("userId") Long userId,
            @Param("status") OrderStatus status
    );
}
