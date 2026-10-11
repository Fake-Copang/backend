package com.example.fake_coupang.placementSpec;

import com.example.fake_coupang.product.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "placement_spec")
@Getter @Setter
@NoArgsConstructor
public class PlacementSpec {

    @Id
    private Long productId;

    @MapsId // Product의 ID를 공유하여 1:1 관계 매핑
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false)
    private PlacementKind kind; // FLOOR(바닥 가구) or WALL(벽 가구)

    @Column(name = "width_cells", nullable = false)
    private Integer widthCells; // 가로 칸 수 (1~8)

    @Column(name = "depth_cells")
    private Integer depthCells; // 바닥 가구의 세로 방향 칸 수 (벽 상품은 NULL)

    @Column(name = "height_cells")
    private Integer heightCells; // 벽 상품의 세로 칸 수 (바닥 가구는 NULL)

    @Column(name = "model_height", precision = 4, scale = 2)
    private BigDecimal modelHeight; // 바닥 가구의 3D 모델 높이

    @Column(name = "color", length = 7, nullable = false)
    private String color; // 3D 박스 색상 (예: #d9c4a3)

    @Column(name = "model_url", length = 500)
    private String modelUrl; // 3D 모델(.glTF) 주소
}