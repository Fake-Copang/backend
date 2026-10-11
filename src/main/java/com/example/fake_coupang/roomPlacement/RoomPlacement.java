package com.example.fake_coupang.roomPlacement;

import com.example.fake_coupang.orderItem.OrderItem;
import com.example.fake_coupang.room.Room;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "room_placement",
        uniqueConstraints = { // 동일한 주문 상품 항목이 하나의 방에 중복으로 배치되는 것을 DB에서 차단
                @UniqueConstraint(
                        name = "uk_room_placement_room_order_item",
                        columnNames = {"room_id", "order_item_id"}
                )
        }
)
@Getter @Setter
@NoArgsConstructor
public class RoomPlacement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "placement_id")
    private Long placementId; // 배치 ID (PK)

    // 방 ID (FK -> room, NOT NULL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    // 배치한 주문 항목 (FK -> order_item, NOT NULL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "surface", nullable = false)
    private PlacementSurface surface; // 바닥 / 뒤쪽 벽 / 왼쪽 벽

    @Column(name = "cell_x", nullable = false)
    private Integer cellX; // 바닥은 x 좌표, 벽은 가로 좌표

    @Column(name = "cell_y", nullable = false)
    private Integer cellY; // 바닥은 z 좌표, 벽은 세로 좌표

    @Column(name = "rotation", nullable = false)
    private Integer rotation = 0; // 회전값(0~3, 기본값 0)
}