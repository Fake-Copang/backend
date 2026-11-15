
package com.example.fake_coupang.room.dto;

public record RoomItemResponse(
        String id,
        String name,
        String kind,
        int w,
        Integer d,
        Integer h,
        Double height,
        String color,
        String image,
        String model,
        boolean surface,
        boolean stackable
) {
}
