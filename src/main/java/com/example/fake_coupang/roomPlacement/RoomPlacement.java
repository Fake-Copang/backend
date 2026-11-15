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
        uniqueConstraints = {
                // 동일한 주문 항목의 특정 수량 단위(unit_index)가 하나의 방에 중복으로 배치되는 것을 방지
                @UniqueConstraint(
                        name = "uk_room_placement_room_order_item_unit",
                        columnNames = {"room_id", "order_item_id", "unit_index"}
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

    @Column(name = "unit_index", nullable = false)
    private Integer unitIndex; // 같은 주문 항목 안에서 몇 번째 물건인지 (1부터 시작)

    @Enumerated(EnumType.STRING)
    @Column(name = "surface", nullable = false)
    private PlacementSurface surface; // 바닥('floor'), 뒤쪽 벽('back'), 왼쪽 벽('left')

    @Column(name = "cell_x", nullable = false)
    private Integer cellX; // 바닥이면 x 좌표(0-7), 벽이면 가로 좌표(0-7)

    @Column(name = "cell_y", nullable = false)
    private Integer cellY; // 바닥이면 z 좌표(0-7), 벽이면 세로 칸(0-4)

    @Column(name = "rotation", nullable = false)
    private Integer rotation = 0; // 회전값(0~3, 기본값 0, 벽 상품은 항상 0)
}