
package com.example.fake_coupang.order;

import com.example.fake_coupang.orderItem.OrderItem;
import com.example.fake_coupang.orderItem.OrderItemRepository;
import com.example.fake_coupang.product.Product;
import com.example.fake_coupang.product.ProductRepository;
import com.example.fake_coupang.user.User;
import com.example.fake_coupang.user.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {

        if (request == null
                || request.userId() == null
                || request.userId() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "잘못된 사용자 ID"
            );
        }

        if (request.items() == null
                || request.items().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "주문 상품이 없습니다"
            );
        }

        User user = userRepository
                .findByIdForUpdate(request.userId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "사용자 없음"
                ));

        List<OrderItem> items = new ArrayList<>();
        Set<Long> productIds = new HashSet<>();

        long totalPrice = 0L;

        for (OrderRequest.OrderItemRequest itemRequest
                : request.items()) {

            if (itemRequest == null
                    || itemRequest.productId() == null
                    || itemRequest.orderCount() == null
                    || itemRequest.orderCount() <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "잘못된 주문 상품 정보"
                );
            }

            if (!productIds.add(itemRequest.productId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "중복된 상품 ID"
                );
            }

            Product product = productRepository
                    .findById(itemRequest.productId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "상품 없음"
                    ));

            if (product.getPrice() == null
                    || product.getPrice() < 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "상품 가격 오류"
                );
            }

            OrderItem orderItem = new OrderItem();

            orderItem.setProduct(product);
            orderItem.setOrderCount(itemRequest.orderCount());
            orderItem.setUnitPrice(
                    product.getPrice().longValue()
            );

            items.add(orderItem);

            try {
                long itemPrice = Math.multiplyExact(
                        orderItem.getUnitPrice(),
                        itemRequest.orderCount().longValue()
                );

                totalPrice = Math.addExact(
                        totalPrice,
                        itemPrice
                );

            } catch (ArithmeticException e) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "주문 금액 범위 초과"
                );
            }
        }

        if (user.getVirtualMoney() < totalPrice) {
            throw new InsufficientBalanceException();
        }

        user.deductVirtualMoney(totalPrice);

        Order order = new Order();

        order.setUser(user);
        order.setTotalPrice(totalPrice);
        order.setOrderedAt(LocalDateTime.now());
        order.setEstimatedDeliveryHours(
                ThreadLocalRandom.current().nextInt(3, 7)
        );
        order.setStatus(OrderStatus.ORDERED);

        orderRepository.save(order);

        for (OrderItem item : items) {
            item.setOrder(order);
        }

        orderItemRepository.saveAll(items);

        userRepository.save(user);

        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {

        return orderRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        return toResponse(findOrder(id));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(
            Long userId
    ) {
        if (userId == null || userId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "잘못된 사용자 ID"
            );
        }

        return orderRepository.findByUser_Id(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OrderResponse updateStatus(
            Long id,
            OrderStatus status
    ) {
        Order order = findOrder(id);

        if (status == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "주문 상태 없음"
            );
        }

        boolean valid =
                (order.getStatus() == OrderStatus.ORDERED
                        && status == OrderStatus.SHIPPING)
                        ||
                        (order.getStatus() == OrderStatus.SHIPPING
                                && status == OrderStatus.DELIVERED);

        if (!valid) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "허용되지 않는 주문 상태 변경"
            );
        }

        order.setStatus(status);

        return toResponse(order);
    }

    private Order findOrder(Long id) {

        return orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "주문 없음"
                ));
    }

    private OrderResponse toResponse(Order order) {

        List<OrderResponse.OrderItemResponse> items =
                orderItemRepository
                        .findByOrder_Id(order.getId())
                        .stream()
                        .map(item ->
                                new OrderResponse.OrderItemResponse(
                                        item.getId(),
                                        item.getProduct().getId(),
                                        item.getProduct().getName(),
                                        item.getOrderCount(),
                                        item.getUnitPrice()
                                )
                        )
                        .toList();

        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getTotalPrice(),
                order.getOrderedAt(),
                order.getEstimatedDeliveryHours(),
                order.getStatus(),
                items
        );
    }
}
