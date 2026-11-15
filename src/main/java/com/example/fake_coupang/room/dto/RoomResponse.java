
package com.example.fake_coupang.room.dto;

import java.util.List;
import java.util.Map;

public record RoomResponse(
        RoomInfo room,
        List<RoomItemResponse> items,
        Map<String, Map<String, Object>> placements
) {
    public record RoomInfo(
            Integer version
    ) {
    }
}
