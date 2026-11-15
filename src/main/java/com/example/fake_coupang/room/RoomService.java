
package com.example.fake_coupang.room;

import com.example.fake_coupang.orderItem.OrderItem;
import com.example.fake_coupang.orderItem.OrderItemRepository;
import com.example.fake_coupang.placementSpec.PlacementKind;
import com.example.fake_coupang.placementSpec.PlacementSpec;
import com.example.fake_coupang.placementSpec.PlacementSpecRepository;
import com.example.fake_coupang.room.dto.PlacementSaveRequest;
import com.example.fake_coupang.room.dto.PlacementSaveResponse;
import com.example.fake_coupang.room.dto.RoomItemResponse;
import com.example.fake_coupang.room.dto.RoomResponse;
import com.example.fake_coupang.roomPlacement.PlacementSurface;
import com.example.fake_coupang.roomPlacement.RoomPlacement;
import com.example.fake_coupang.roomPlacement.RoomPlacementRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private static final int GRID = 8;
    private static final int WALL_ROWS = 5;

    private final RoomRepository roomRepository;
    private final RoomPlacementRepository placementRepository;
    private final PlacementSpecRepository specRepository;
    private final OrderItemRepository orderItemRepository;

    public RoomService(
            RoomRepository roomRepository,
            RoomPlacementRepository placementRepository,
            PlacementSpecRepository specRepository,
            OrderItemRepository orderItemRepository
    ) {
        this.roomRepository = roomRepository;
        this.placementRepository = placementRepository;
        this.specRepository = specRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public static class RoomApiException extends RuntimeException {
        private final HttpStatus status;
        private final String code;
        private final String itemId;

        public RoomApiException(
                HttpStatus status,
                String code,
                String message,
                String itemId
        ) {
            super(message);
            this.status = status;
            this.code = code;
            this.itemId = itemId;
        }

        public HttpStatus getStatus() {
            return status;
        }

        public String getCode() {
            return code;
        }

        public String getItemId() {
            return itemId;
        }
    }

    private RoomApiException bad(String code, String itemId) {
        return new RoomApiException(
                HttpStatus.BAD_REQUEST,
                code,
                "올바르지 않은 방 배치입니다: " + code,
                itemId
        );
    }

    private Room getOrCreateRoom() {
        return roomRepository.findFirstByOrderByRoomIdAsc()
                .orElseGet(() -> {
                    Room room = new Room();
                    room.setVersion(0);
                    room.setUpdatedAt(LocalDateTime.now());
                    return roomRepository.saveAndFlush(room);
                });
    }

    @Transactional
    public RoomResponse getRoom() {
        Room room = getOrCreateRoom();

        Map<Long, OrderItem> owned = getOrderItems();

        List<PlacementSpec> specs = specRepository.findAll()
                .stream()
                .filter(s -> owned.containsKey(s.getProductId()))
                .sorted(Comparator.comparing(PlacementSpec::getProductId))
                .toList();

        List<RoomItemResponse> items = specs.stream()
                .map(this::toItemResponse)
                .toList();

        Map<String, Map<String, Object>> placements =
                new LinkedHashMap<>();

        for (RoomPlacement rp :
                placementRepository.findByRoom_RoomId(room.getRoomId())) {

            String id = rp.getOrderItem()
                    .getProduct().getId().toString();

            Map<String, Object> p = new LinkedHashMap<>();

            if (rp.getSurface() == PlacementSurface.FLOOR) {
                p.put("x", rp.getCellX());
                p.put("z", rp.getCellY());
                p.put("rot", rp.getRotation());
            } else {
                p.put("wall",
                        rp.getSurface() == PlacementSurface.BACK_WALL
                                ? "back" : "left");
                p.put("col", rp.getCellX());
                p.put("row", rp.getCellY());
            }

            if (placements.putIfAbsent(id, p) != null) {
                throw new RoomApiException(
                        HttpStatus.CONFLICT,
                        "DUPLICATE_PRODUCT",
                        "동일 상품의 배치가 중복되어 있습니다.",
                        id
                );
            }
        }

        return new RoomResponse(
                new RoomResponse.RoomInfo(room.getVersion()),
                items,
                placements
        );
    }

    private RoomItemResponse toItemResponse(PlacementSpec spec) {
        var product = spec.getProduct();
        boolean floor = spec.getKind() == PlacementKind.FLOOR;

        return new RoomItemResponse(
                product.getId().toString(),
                product.getName(),
                floor ? "floor" : "wall",
                spec.getWidthCells(),
                floor ? spec.getDepthCells() : null,
                floor ? null : spec.getHeightCells(),
                floor && spec.getModelHeight() != null
                        ? spec.getModelHeight().doubleValue()
                        : null,
                spec.getColor(),
                product.getImageUrl(),
                spec.getModelUrl(),
                spec.isSurface(),
                spec.isStackable()
        );
    }

    private Map<Long, OrderItem> getOrderItems() {
        return orderItemRepository.findAll().stream()
                .sorted(Comparator.comparing(OrderItem::getId))
                .collect(Collectors.toMap(
                        item -> item.getProduct().getId(),
                        Function.identity(),
                        (first, second) -> first,
                        LinkedHashMap::new
                ));
    }

    private record Rect(int x, int z, int w, int d) {
        boolean contains(Rect b) {
            return b.x >= x && b.z >= z
                    && b.x + b.w <= x + w
                    && b.z + b.d <= z + d;
        }

        boolean overlaps(Rect b) {
            return x < b.x + b.w
                    && x + w > b.x
                    && z < b.z + b.d
                    && z + d > b.z;
        }
    }

    private record Candidate(
            String id,
            PlacementSpec spec,
            PlacementSaveRequest.Placement request,
            PlacementSurface surface,
            Rect rect
    ) {
    }

    @Transactional
    public PlacementSaveResponse save(
            PlacementSaveRequest request
    ) {
        if (request == null
                || request.version() == null
                || request.placements() == null) {
            throw bad("INVALID_REQUEST", null);
        }

        Room room = getOrCreateRoom();

        if (!Objects.equals(room.getVersion(), request.version())) {
            throw new RoomApiException(
                    HttpStatus.CONFLICT,
                    "VERSION_CONFLICT",
                    "다른 곳에서 배치가 변경되었습니다.",
                    null
            );
        }

        Map<Long, PlacementSpec> specs =
                specRepository.findAll().stream()
                        .collect(Collectors.toMap(
                                PlacementSpec::getProductId,
                                Function.identity()
                        ));

        Map<Long, OrderItem> owned = getOrderItems();
        List<Candidate> candidates = new ArrayList<>();

        for (var entry : request.placements().entrySet()) {
            String id = entry.getKey();
            var p = entry.getValue();

            Long productId;

            try {
                productId = Long.valueOf(id);
            } catch (NumberFormatException e) {
                throw bad("INVALID_ITEM_ID", id);
            }

            PlacementSpec spec = specs.get(productId);

            if (spec == null) {
                throw bad("UNKNOWN_ITEM", id);
            }

            if (!owned.containsKey(productId)) {
                throw bad("ITEM_NOT_OWNED", id);
            }

            if (p == null) {
                throw bad("INVALID_POSITION", id);
            }

            candidates.add(parseCandidate(id, spec, p));
        }

        validate(candidates);

        List<RoomPlacement> old =
                placementRepository.findByRoom_RoomId(room.getRoomId());

        placementRepository.deleteAll(old);
        placementRepository.flush();

        List<RoomPlacement> next = new ArrayList<>();

        for (Candidate c : candidates) {
            Long productId = Long.valueOf(c.id());

            RoomPlacement rp = new RoomPlacement();
            rp.setRoom(room);
            rp.setOrderItem(owned.get(productId));
            rp.setSurface(c.surface());

            if (c.surface() == PlacementSurface.FLOOR) {
                rp.setCellX(c.request().x());
                rp.setCellY(c.request().z());
                rp.setRotation(c.request().rot());
            } else {
                rp.setCellX(c.request().col());
                rp.setCellY(c.request().row());
                rp.setRotation(0);
            }

            next.add(rp);
        }

        placementRepository.saveAll(next);

        room.setUpdatedAt(LocalDateTime.now());
        roomRepository.saveAndFlush(room);

        return new PlacementSaveResponse(room.getVersion());
    }

    private Candidate parseCandidate(
            String id,
            PlacementSpec spec,
            PlacementSaveRequest.Placement p
    ) {
        if (spec.getWidthCells() == null
                || spec.getWidthCells() <= 0) {
            throw bad("INVALID_SPEC", id);
        }

        if (spec.getKind() == PlacementKind.FLOOR) {
            if (p.x() == null || p.z() == null
                    || p.rot() == null
                    || p.rot() < 0 || p.rot() > 3
                    || spec.getDepthCells() == null
                    || spec.getDepthCells() <= 0
                    || p.wall() != null
                    || p.col() != null
                    || p.row() != null) {
                throw bad("INVALID_POSITION", id);
            }

            int w = spec.getWidthCells();
            int d = spec.getDepthCells();

            if (p.rot() % 2 != 0) {
                int tmp = w;
                w = d;
                d = tmp;
            }

            Rect rect = new Rect(p.x(), p.z(), w, d);

            if (rect.x() < 0 || rect.z() < 0
                    || (long) rect.x() + w > GRID
                    || (long) rect.z() + d > GRID) {
                throw bad("OUTSIDE_ROOM", id);
            }

            return new Candidate(
                    id, spec, p, PlacementSurface.FLOOR, rect
            );
        }

        if (spec.getKind() == PlacementKind.WALL) {
            if (p.wall() == null
                    || p.col() == null
                    || p.row() == null
                    || p.x() != null
                    || p.z() != null
                    || p.rot() != null
                    || spec.getHeightCells() == null
                    || spec.getHeightCells() <= 0) {
                throw bad("INVALID_POSITION", id);
            }

            PlacementSurface surface = switch (p.wall()) {
                case "back" -> PlacementSurface.BACK_WALL;
                case "left" -> PlacementSurface.LEFT_WALL;
                default -> throw bad("INVALID_WALL", id);
            };

            Rect rect = new Rect(
                    p.col(), p.row(),
                    spec.getWidthCells(),
                    spec.getHeightCells()
            );

            if (rect.x() < 0 || rect.z() < 0
                    || (long) rect.x() + rect.w() > GRID
                    || (long) rect.z() + rect.d() > WALL_ROWS) {
                throw bad("OUTSIDE_ROOM", id);
            }

            return new Candidate(id, spec, p, surface, rect);
        }

        throw bad("INVALID_KIND", id);
    }

    private void validate(List<Candidate> candidates) {

        Map<String, String> layers = new HashMap<>();

        for (Candidate c : candidates) {
            if (c.surface() != PlacementSurface.FLOOR) {
                layers.put(c.id(), c.surface().name());
                continue;
            }

            String layer = "FLOOR";

            if (c.spec().isStackable()) {
                List<Candidate> bases = candidates.stream()
                        .filter(b -> !b.id().equals(c.id()))
                        .filter(b -> b.surface() == PlacementSurface.FLOOR)
                        .filter(b -> b.spec().isSurface())
                        .filter(b -> b.rect().contains(c.rect()))
                        .toList();

                if (bases.size() > 1) {
                    throw bad("AMBIGUOUS_SURFACE", c.id());
                }

                if (!bases.isEmpty()) {
                    layer = "TOP:" + bases.get(0).id();
                }
            }

            layers.put(c.id(), layer);
        }

        for (int i = 0; i < candidates.size(); i++) {
            Candidate a = candidates.get(i);

            for (int j = i + 1; j < candidates.size(); j++) {
                Candidate b = candidates.get(j);

                if (!layers.get(a.id()).equals(layers.get(b.id()))) {
                    continue;
                }

                if (a.rect().overlaps(b.rect())) {
                    throw bad("OVERLAP", b.id());
                }
            }
        }
    }
}
