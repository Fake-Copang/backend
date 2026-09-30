package com.example.fake_coupang.product;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    // DummyJSON 등 외부 오픈소스 API에서 가져온 더미 데이터의 ID
    @Column(name = "dummy_id", nullable = false)
    private Long dummyId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Builder
    public Product(Long dummyId, String name, Integer price, Integer quantity) {
        this.dummyId = dummyId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    // 수량 변경이나 정보 수정을 위한 비즈니스 메서드예시
    public void updateQuantity(Integer quantity) {this.quantity = quantity;}
    public void updatePrice(Integer price) {this.price = price;}
    public void updateName(String name) {this.name = name;}
}