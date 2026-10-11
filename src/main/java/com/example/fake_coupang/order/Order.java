package com.example.fake_coupang.order;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "com/example/fake_coupang/order")
@Getter @Setter
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    // 사용자 ID (FK 연관관계를 객체로 매핑하려면 @ManyToOne을 사용)
//    @Column(name = "user_id", nullable = false)
//    private Long userId;

    @Column(name = "total_price", nullable = false)
    private Long totalPrice;

    @Column(name = "ordered_at", nullable = false)
    private LocalDateTime orderedAt;

    @Column(name = "estimated_delivery_hours", nullable = false)
    private Integer estimatedDeliveryHours; // 예상 배송 시간 (3~6시간)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status; // ORDERED(주문 완료), SHIPPING(배송 중), DELIVERED(배송 완료)
}