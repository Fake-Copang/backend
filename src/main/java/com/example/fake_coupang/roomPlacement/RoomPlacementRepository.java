
package com.example.fake_coupang.roomPlacement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomPlacementRepository
        extends JpaRepository<RoomPlacement, Long> {

    List<RoomPlacement> findByRoom_RoomId(Long roomId);
}
