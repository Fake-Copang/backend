
package com.example.fake_coupang.room;

import com.example.fake_coupang.room.dto.PlacementSaveRequest;
import com.example.fake_coupang.room.dto.PlacementSaveResponse;
import com.example.fake_coupang.room.dto.RoomResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/room")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public RoomResponse getRoom(
            @RequestParam Long userId
    ) {
        return roomService.getRoom(userId);
    }

    @PutMapping("/placements")
    public PlacementSaveResponse savePlacements(
            @RequestParam Long userId,
            @RequestBody PlacementSaveRequest request
    ) {
        return roomService.save(userId, request);
    }

    @ExceptionHandler(RoomService.RoomApiException.class)
    public ResponseEntity<Map<String, Object>> handleRoomError(
            RoomService.RoomApiException e
    ) {
        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put("message", e.getMessage());
        body.put("code", e.getCode());

        if (e.getItemId() != null) {
            body.put("itemId", e.getItemId());
        }

        return ResponseEntity
                .status(e.getStatus())
                .body(body);
    }
}
