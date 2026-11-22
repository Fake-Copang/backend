
package com.example.fake_coupang.order;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderDeliveryScheduler {

    private final OrderRepository orderRepository;

    public OrderDeliveryScheduler(
            OrderRepository orderRepository
    ) {
        this.orderRepository = orderRepository;
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void updateDeliveryStatuses() {

        List<Order> orders = orderRepository.findByStatusIn(
                List.of(
                        OrderStatus.ORDERED,
                        OrderStatus.SHIPPING
                )
        );

        LocalDateTime now = LocalDateTime.now();

        for (Order order : orders) {

            if (order.getOrderedAt() == null
                    || order.getEstimatedDeliveryHours() == null
                    || order.getEstimatedDeliveryHours() <= 0) {
                continue;
            }

            long totalSeconds =
                    order.getEstimatedDeliveryHours() * 3600L;

            long elapsedSeconds = Duration.between(
                    order.getOrderedAt(),
                    now
            ).getSeconds();

            if (elapsedSeconds < 0) {
                continue;
            }

            if (elapsedSeconds >= totalSeconds) {
                order.setStatus(OrderStatus.DELIVERED);
            } else if (
                    elapsedSeconds * 10 >= totalSeconds * 3
                            && order.getStatus() == OrderStatus.ORDERED
            ) {
                order.setStatus(OrderStatus.SHIPPING);
            }
        }
    }
}
