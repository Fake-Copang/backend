package com.example.fake_coupang.orderItem;

import com.example.fake_coupang.order.Order;
import com.example.fake_coupang.product.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_item")
@Getter @Setter
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주문 마스터와 연관관계 매핑 (FK -> orders)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // 상품과 연관관계 매핑 (FK -> product)
    // Product : OrderItem (1:N)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "order_count", nullable = false)
    private Integer orderCount; // 총 몇 세트를 주문했는지?

    @Column(name = "unit_price", nullable = false)
    private Long unitPrice; // 구매 당시 개당 가격 (상품 가격이 변동되어도 기존 주문 금액 유지)
}