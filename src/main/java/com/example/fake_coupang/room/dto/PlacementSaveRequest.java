
package com.example.fake_coupang.room.dto;

import java.util.Map;

public record PlacementSaveRequest(
        Integer version,
        Map<String, Placement> placements
) {
    public record Placement(
            Integer x,
            Integer z,
            Integer rot,
            String wall,
            Integer col,
            Integer row
    ) {
    }
}
