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

    // 가로/높이/깊이 (상품의 3D 크기)
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

    // --- 새로 추가된 방 배치 관련 컬럼들 ---
    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false)
    private ProductKind kind = ProductKind.FLOOR; // 바닥에 놓는 상품 / 벽에 거는 상품 (기본값 'floor')

    @Column(name = "is_surface", nullable = false)
    private Boolean isSurface = false; // 위에 물건을 올릴 수 있는 가구 (침대, 책상 등)

    @Column(name = "stackable", nullable = false)
    private Boolean stackable = false; // 가구 위에 올라갈 수 있는 작은 물건 (신발, 노트북 등)
    // -------------------------------------------------------------

    // Supabase(PostgreSQL)의 json 또는 json 컬럼과 매핑 (NULL 허용)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "json")
    private List<String> tags;

    @Builder
    public Product(Long dummyId, String name, Integer price, Integer quantity,
                   ProductKind kind, Boolean isSurface, Boolean stackable,
                   String modelUrl, String imageUrl, String category,
                   Integer sizeX, Integer sizeY, Integer sizeZ) {
        this.dummyId = dummyId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.kind = kind != null ? kind : ProductKind.FLOOR;
        this.isSurface = isSurface != null ? isSurface : false;
        this.stackable = stackable != null ? stackable : false;
        this.modelUrl = modelUrl;
        this.imageUrl = imageUrl;
        this.category = category;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
    }

    // 기존 비즈니스 수정 메서드들
    public void updateQuantity(Integer quantity) { this.quantity = quantity; }
    public void updatePrice(Integer price) { this.price = price; }
    public void updateName(String name) { this.name = name; }

    public void updateSize(Integer sizeX, Integer sizeY, Integer sizeZ) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
    }

    public void updateModelUrl(String modelUrl) { this.modelUrl = modelUrl; }
    public void updateImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void updateCategory(String category) { this.category = category; }
    public void updateTags(List<String> tags) { this.tags = tags; }

    // 새로 추가된 배치 속성 수정 메서드
    public void updateKind(ProductKind kind) { this.kind = kind; }
    public void updateIsSurface(Boolean isSurface) { this.isSurface = isSurface; }
    public void updateStackable(Boolean stackable) { this.stackable = stackable; }
}