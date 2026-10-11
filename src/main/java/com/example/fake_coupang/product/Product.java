
package com.example.fake_coupang.product;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

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

    // 가로/높이/깊이 (상품의 3D 크기)  =>  추후 NOT NULL로 변경
    @Column(name = "size_x")
    private Integer sizeX;

    @Column(name = "size_y")
    private Integer sizeY;

    @Column(name = "size_z")
    private Integer sizeZ;

    @Column(name = "model_url")
    private String modelUrl;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "category")
    private String category;

    // Supabase(PostgreSQL)의 json 또는 json 컬럼과 매핑 (NULL 허용)
    // @JdbcTypeCode(SqlTypes.JSON):
    // 하이버네이트가 자바의 List<String>을 자동으로 JSON 문자열로 직렬화(Serialization)하고,
    // DB에서 읽을 때 다시 List<String>으로 역직렬화
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "json")
    private List<String> tags;

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

    // 상품 3D 크기 수정
    public void updateSize(Integer sizeX, Integer sizeY, Integer sizeZ) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
    }

    // 상품 3D 모델 URL 수정
    public void updateModelUrl(String modelUrl) {
        this.modelUrl = modelUrl;
    }

    // 상품 이미지 URL 수정
    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // 상품 카테고리 수정
    public void updateCategory(String category) {
        this.category = category;
    }

    // 상품 태그 수정
    public void updateTags(List<String> tags) {
        this.tags = tags;
    }

}
