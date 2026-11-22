
package com.example.fake_coupang.room;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomRepository
        extends JpaRepository<Room, Long> {

    Optional<Room> findFirstByOrderByRoomIdAsc();

    Optional<Room> findByUserId(Long userId);
}
