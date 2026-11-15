
package com.example.fake_coupang.placementSpec;

import com.example.fake_coupang.product.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "placement_spec")
@Getter
@Setter
@NoArgsConstructor
public class PlacementSpec {

    @Id
    private Long productId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false)
    private PlacementKind kind;

    @Column(name = "width_cells", nullable = false)
    private Integer widthCells;

    @Column(name = "depth_cells")
    private Integer depthCells;

    @Column(name = "height_cells")
    private Integer heightCells;

    @Column(name = "model_height", precision = 4, scale = 2)
    private BigDecimal modelHeight;

    @Column(name = "color", length = 7, nullable = false)
    private String color;

    @Column(name = "model_url", length = 500)
    private String modelUrl;

    @Column(name = "is_surface", nullable = false)
    private boolean surface = false;

    @Column(name = "stackable", nullable = false)
    private boolean stackable = false;
}
