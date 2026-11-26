
package com.example.fake_coupang.room;

import com.example.fake_coupang.order.OrderStatus;
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
import com.example.fake_coupang.user.User;
import com.example.fake_coupang.user.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    private final UserRepository userRepository;

    public RoomService(
            RoomRepository roomRepository,
            RoomPlacementRepository placementRepository,
            PlacementSpecRepository specRepository,
            OrderItemRepository orderItemRepository,
            UserRepository userRepository
    ) {
        this.roomRepository = roomRepository;
        this.placementRepository = placementRepository;
        this.specRepository = specRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
    }

    public static class RoomApiException
            extends RuntimeException {

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

    private RoomApiException bad(
            String code,
            String itemId
    ) {
        return new RoomApiException(
                HttpStatus.BAD_REQUEST,
                code,
                "올바르지 않은 방 배치입니다: " + code,
                itemId
        );
    }

    private void validateUser(Long userId) {

        if (userId == null || userId <= 0) {
            throw bad("INVALID_USER_ID", null);
        }

        if (!userRepository.existsById(userId)) {
            throw new RoomApiException(
                    HttpStatus.NOT_FOUND,
                    "USER_NOT_FOUND",
                    "사용자를 찾을 수 없습니다.",
                    null
            );
        }
    }

    private Room getOrCreateRoom(Long userId) {

        return roomRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "사용자를 찾을 수 없습니다."
                            ));

                    Room room = new Room();
                    room.setUser(user);
                    room.setUpdatedAt(LocalDateTime.now());

                    return roomRepository.saveAndFlush(room);
                });
    }

    private Map<Long, OrderItem> getOrderItems(
            Long userId
    ) {
        return orderItemRepository
                .findByUserIdAndOrderStatus(
                        userId,
                        OrderStatus.DELIVERED
                )
                .stream()
                .collect(Collectors.toMap(
                        OrderItem::getId,
                        Function.identity(),
                        (first, second) -> first,
                        LinkedHashMap::new
                ));
    }

    private Map<Long, PlacementSpec> getSpecs() {

        return specRepository.findAll()
                .stream()
                .collect(Collectors.toMap(
                        PlacementSpec::getProductId,
                        Function.identity()
                ));
    }

    @Transactional
    public RoomResponse getRoom(Long userId) {

        validateUser(userId);

        Room room = getOrCreateRoom(userId);

        Map<Long, OrderItem> owned =
                getOrderItems(userId);

        Map<Long, PlacementSpec> specs =
                getSpecs();

        List<RoomItemResponse> items =
                new ArrayList<>();

        for (OrderItem orderItem : owned.values()) {

            Long productId =
                    orderItem.getProduct().getId();

            PlacementSpec spec =
                    specs.get(productId);

            if (spec == null) {
                continue;
            }

            items.add(toItemResponse(orderItem, spec));
        }

        Map<String, Map<String, Object>> placements =
                new LinkedHashMap<>();

        List<RoomPlacement> saved =
                placementRepository.findByRoom_RoomId(
                        room.getRoomId()
                );

        for (RoomPlacement rp : saved) {

            Long orderItemId =
                    rp.getOrderItem().getId();

            if (!owned.containsKey(orderItemId)) {
                continue;
            }

            String id = orderItemId.toString();

            Map<String, Object> p =
                    new LinkedHashMap<>();

            if (rp.getSurface() == PlacementSurface.FLOOR) {

                p.put("x", rp.getCellX());
                p.put("z", rp.getCellY());
                p.put("rot", rp.getRotation());

            } else {

                p.put(
                        "wall",
                        rp.getSurface() == PlacementSurface.BACK
                                ? "back"
                                : "left"
                );

                p.put("col", rp.getCellX());
                p.put("row", rp.getCellY());
            }

            if (placements.putIfAbsent(id, p) != null) {
                throw new RoomApiException(
                        HttpStatus.CONFLICT,
                        "DUPLICATE_ORDER_ITEM",
                        "주문 항목의 배치가 중복되어 있습니다.",
                        id
                );
            }
        }

        return new RoomResponse(
                new RoomResponse.RoomInfo(
                        room.getVersion()
                ),
                items,
                placements
        );
    }

    private RoomItemResponse toItemResponse(
            OrderItem orderItem,
            PlacementSpec spec
    ) {
        var product = orderItem.getProduct();

        boolean floor =
                spec.getKind() == PlacementKind.FLOOR;

        return new RoomItemResponse(
                orderItem.getId().toString(),
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

    private record Rect(
            int x,
            int z,
            int w,
            int d
    ) {
        boolean contains(Rect b) {
            return b.x >= x
                    && b.z >= z
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
            OrderItem orderItem,
            PlacementSpec spec,
            PlacementSaveRequest.Placement request,
            PlacementSurface surface,
            Rect rect
    ) {
    }

    @Transactional
    public PlacementSaveResponse save(
            Long userId,
            PlacementSaveRequest request
    ) {
        validateUser(userId);

        if (request == null
                || request.version() == null
                || request.placements() == null) {
            throw bad("INVALID_REQUEST", null);
        }

        Room room = getOrCreateRoom(userId);

        if (!Objects.equals(
                room.getVersion(),
                request.version()
        )) {
            throw new RoomApiException(
                    HttpStatus.CONFLICT,
                    "VERSION_CONFLICT",
                    "다른 곳에서 배치가 변경되었습니다.",
                    null
            );
        }

        Map<Long, PlacementSpec> specs =
                getSpecs();

        Map<Long, OrderItem> owned =
                getOrderItems(userId);

        List<Candidate> candidates =
                new ArrayList<>();

        for (var entry : request.placements().entrySet()) {

            String id = entry.getKey();

            var p = entry.getValue();

            Long orderItemId;

            try {
                orderItemId = Long.valueOf(id);
            } catch (NumberFormatException e) {
                throw bad("INVALID_ITEM_ID", id);
            }

            OrderItem orderItem = owned.get(orderItemId);

            if (orderItem == null) {
                throw bad("ITEM_NOT_OWNED", id);
            }

            Long productId =
                    orderItem.getProduct().getId();

            PlacementSpec spec =
                    specs.get(productId);

            if (spec == null) {
                throw bad("UNKNOWN_ITEM", id);
            }

            if (p == null) {
                throw bad("INVALID_POSITION", id);
            }

            candidates.add(
                    parseCandidate(
                            id,
                            orderItem,
                            spec,
                            p
                    )
            );
        }

        validate(candidates);

        List<RoomPlacement> old =
                placementRepository.findByRoom_RoomId(
                        room.getRoomId()
                );

        placementRepository.deleteAll(old);
        placementRepository.flush();

        List<RoomPlacement> next =
                new ArrayList<>();

        for (Candidate c : candidates) {

            RoomPlacement rp =
                    new RoomPlacement();

            rp.setRoom(room);
            rp.setOrderItem(c.orderItem());
            rp.setUnitIndex(1);
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

        return new PlacementSaveResponse(
                room.getVersion()
        );
    }

    private Candidate parseCandidate(
            String id,
            OrderItem orderItem,
            PlacementSpec spec,
            PlacementSaveRequest.Placement p
    ) {
        if (spec.getWidthCells() == null
                || spec.getWidthCells() <= 0) {
            throw bad("INVALID_SPEC", id);
        }

        if (spec.getKind() == PlacementKind.FLOOR) {

            if (p.x() == null
                    || p.z() == null
                    || p.rot() == null
                    || p.rot() < 0
                    || p.rot() > 3
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

            Rect rect = new Rect(
                    p.x(),
                    p.z(),
                    w,
                    d
            );

            if (rect.x() < 0
                    || rect.z() < 0
                    || (long) rect.x() + w > GRID
                    || (long) rect.z() + d > GRID) {
                throw bad("OUTSIDE_ROOM", id);
            }

            return new Candidate(
                    id,
                    orderItem,
                    spec,
                    p,
                    PlacementSurface.FLOOR,
                    rect
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

            PlacementSurface surface =
                    switch (p.wall()) {
                        case "back" -> PlacementSurface.BACK;
                        case "left" -> PlacementSurface.LEFT;
                        default -> throw bad(
                                "INVALID_WALL",
                                id
                        );
                    };

            Rect rect = new Rect(
                    p.col(),
                    p.row(),
                    spec.getWidthCells(),
                    spec.getHeightCells()
            );

            if (rect.x() < 0
                    || rect.z() < 0
                    || (long) rect.x() + rect.w() > GRID
                    || (long) rect.z() + rect.d() > WALL_ROWS) {
                throw bad("OUTSIDE_ROOM", id);
            }

            return new Candidate(
                    id,
                    orderItem,
                    spec,
                    p,
                    surface,
                    rect
            );
        }

        throw bad("INVALID_KIND", id);
    }

    private void validate(
            List<Candidate> candidates
    ) {
        Map<String, String> layers =
                new HashMap<>();

        for (Candidate c : candidates) {

            if (c.surface() != PlacementSurface.FLOOR) {

                layers.put(
                        c.id(),
                        c.surface().name()
                );

                continue;
            }

            String layer = "floor";

            if (c.spec().isStackable()) {

                boolean hasBase = candidates.stream()
                        .filter(b ->
                                !b.id().equals(c.id())
                        )
                        .filter(b ->
                                b.surface() == PlacementSurface.FLOOR
                        )
                        .filter(b ->
                                b.spec().isSurface()
                        )
                        .anyMatch(b ->
                                b.rect().contains(c.rect())
                        );

                if (hasBase) {
                    layer = "top";
                }
            }

            layers.put(c.id(), layer);
        }

        for (int i = 0; i < candidates.size(); i++) {

            Candidate a = candidates.get(i);

            for (int j = i + 1;
                 j < candidates.size();
                 j++) {

                Candidate b = candidates.get(j);

                if (!layers.get(a.id())
                        .equals(layers.get(b.id()))) {
                    continue;
                }

                if (a.rect().overlaps(b.rect())) {
                    throw bad("OVERLAP", b.id());
                }
            }
        }
    }
}
