package com.example.fake_coupang.roomPlacement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PlacementSurface {
    FLOOR("floor"),      // 바닥
    BACK("back"),        // 뒤쪽 벽
    LEFT("left");        // 왼쪽 벽

    private final String value;

    PlacementSurface(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PlacementSurface fromValue(String value) {
        for (PlacementSurface surface : PlacementSurface.values()) {
            if (surface.value.equalsIgnoreCase(value)) {
                return surface;
            }
        }
        throw new IllegalArgumentException("Unknown surface value: " + value);
    }
}