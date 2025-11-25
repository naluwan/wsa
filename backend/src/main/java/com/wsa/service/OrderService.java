package com.wsa.service;

import com.wsa.dto.CreateOrderResponse;
import com.wsa.dto.OrderResponseDto;
import com.wsa.dto.PayOrderResponse;
import com.wsa.entity.Course;
import com.wsa.entity.Order;
import com.wsa.entity.UserCourse;
import com.wsa.repository.CourseRepository;
import com.wsa.repository.OrderRepository;
import com.wsa.repository.UserCourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * R1.5: 訂單服務
 * 處理訂單建立、付款、查詢等業務邏輯
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CourseRepository courseRepository;
    private final UserCourseRepository userCourseRepository;

    private static final DateTimeFormatter ORDER_NO_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Random RANDOM = new Random();

    /**
     * 生成訂單編號
     * 格式：YYYYMMDDHHmmss + 4位十六進位亂碼
     * 例如：20251121160940a6e5
     */
    private String generateOrderNo() {
        String datePart = LocalDateTime.now().format(ORDER_NO_DATE_FORMAT);
        String randomPart = String.format("%04x", RANDOM.nextInt(0x10000)); // 0000-ffff
        return datePart + randomPart;
    }

    /**
     * 建立訂單
     *
     * @param userId 使用者 ID
     * @param courseId 課程 ID
     * @return 訂單編號
     */
    @Transactional
    public CreateOrderResponse createOrder(UUID userId, UUID courseId) {
        log.info("[OrderService] 建立訂單: userId={}, courseId={}", userId, courseId);

        // 查詢課程
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("課程不存在"));

        // 檢查是否已購買
        boolean alreadyPurchased = userCourseRepository.findByUserIdAndCourseId(userId, courseId).isPresent();
        if (alreadyPurchased) {
            throw new RuntimeException("您已購買此課程");
        }

        // 生成訂單編號
        String orderNo = generateOrderNo();

        // 計算付款期限（3天後）
        LocalDateTime payDeadline = LocalDateTime.now().plusDays(3);

        // 建立訂單
        Order order = Order.builder()
                .orderNo(orderNo)
                .userId(userId)
                .courseId(courseId)
                .amount(course.getPriceTwd())
                .status(Order.Status.PENDING)
                .payDeadline(payDeadline)
                .build();

        orderRepository.save(order);

        log.info("[OrderService] 訂單建立成功: orderNo={}", orderNo);

        return CreateOrderResponse.builder()
                .orderNo(orderNo)
                .build();
    }

    /**
     * 取得單一訂單
     *
     * @param orderNo 訂單編號
     * @return 訂單資料
     */
    public OrderResponseDto getOrder(String orderNo) {
        log.info("[OrderService] 查詢訂單: orderNo={}", orderNo);

        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new RuntimeException("訂單不存在"));

        // 檢查是否過期
        if (Order.Status.PENDING.equals(order.getStatus()) &&
                LocalDateTime.now().isAfter(order.getPayDeadline())) {
            cancelExpiredOrder(order);
        }

        Course course = courseRepository.findById(order.getCourseId())
                .orElseThrow(() -> new RuntimeException("課程不存在"));

        return OrderResponseDto.from(order, course.getTitle());
    }

    /**
     * 訂單付款（模擬）
     *
     * @param orderNo 訂單編號
     * @param userId 使用者 ID
     * @return 付款結果
     */
    @Transactional
    public PayOrderResponse payOrder(String orderNo, UUID userId) {
        log.info("[OrderService] 訂單付款: orderNo={}, userId={}", orderNo, userId);

        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new RuntimeException("訂單不存在"));

        // 驗證訂單所有者
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("無權限操作此訂單");
        }

        // 檢查訂單狀態
        if (Order.Status.PAID.equals(order.getStatus())) {
            throw new RuntimeException("訂單已付款");
        }

        if (Order.Status.CANCELLED.equals(order.getStatus())) {
            throw new RuntimeException("訂單已取消");
        }

        // 檢查是否過期
        if (LocalDateTime.now().isAfter(order.getPayDeadline())) {
            cancelExpiredOrder(order);
            throw new RuntimeException("訂單已過期");
        }

        // 模擬付款成功
        order.setStatus(Order.Status.PAID);
        order.setPaidAt(LocalDateTime.now());
        order.setMemo(null); // 清空備註
        orderRepository.save(order);

        // 建立使用者課程關聯（購買成功）
        UserCourse userCourse = UserCourse.builder()
                .userId(order.getUserId())
                .courseId(order.getCourseId())
                .build();
        userCourseRepository.save(userCourse);

        log.info("[OrderService] 訂單付款成功: orderNo={}", orderNo);

        return PayOrderResponse.builder()
                .status(Order.Status.PAID)
                .paidAt(order.getPaidAt())
                .build();
    }

    /**
     * 查詢某課程的所有訂單
     *
     * @param courseId 課程 ID
     * @return 訂單列表
     */
    public List<OrderResponseDto> getCourseOrders(UUID courseId) {
        log.info("[OrderService] 查詢課程訂單: courseId={}", courseId);

        List<Order> orders = orderRepository.findByCourseIdOrderByCreatedAtDesc(courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("課程不存在"));

        return orders.stream()
                .map(order -> {
                    // 檢查是否過期
                    if (Order.Status.PENDING.equals(order.getStatus()) &&
                            LocalDateTime.now().isAfter(order.getPayDeadline())) {
                        cancelExpiredOrder(order);
                    }
                    return OrderResponseDto.from(order, course.getTitle());
                })
                .collect(Collectors.toList());
    }

    /**
     * 查詢使用者的所有訂單
     *
     * @param userId 使用者 ID
     * @return 訂單列表
     */
    public List<OrderResponseDto> getUserOrders(UUID userId) {
        log.info("[OrderService] 查詢使用者訂單: userId={}", userId);

        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return orders.stream()
                .map(order -> {
                    // 檢查是否過期
                    if (Order.Status.PENDING.equals(order.getStatus()) &&
                            LocalDateTime.now().isAfter(order.getPayDeadline())) {
                        cancelExpiredOrder(order);
                    }

                    Course course = courseRepository.findById(order.getCourseId())
                            .orElseThrow(() -> new RuntimeException("課程不存在"));

                    return OrderResponseDto.from(order, course.getTitle());
                })
                .collect(Collectors.toList());
    }

    /**
     * 查詢使用者的未完成訂單（pending 且尚未過期）
     *
     * @param userId 使用者 ID
     * @return 訂單列表
     */
    public List<OrderResponseDto> getPendingOrders(UUID userId) {
        log.info("[OrderService] 查詢未完成訂單: userId={}", userId);

        List<Order> orders = orderRepository.findPendingOrdersByUserId(
                userId,
                Order.Status.PENDING,
                LocalDateTime.now()
        );

        return orders.stream()
                .map(order -> {
                    Course course = courseRepository.findById(order.getCourseId())
                            .orElseThrow(() -> new RuntimeException("課程不存在"));
                    return OrderResponseDto.from(order, course.getTitle());
                })
                .collect(Collectors.toList());
    }

    /**
     * 取消過期訂單
     *
     * @param order 訂單
     */
    @Transactional
    public void cancelExpiredOrder(Order order) {
        if (!Order.Status.PENDING.equals(order.getStatus())) {
            return;
        }

        log.info("[OrderService] 取消過期訂單: orderNo={}", order.getOrderNo());

        order.setStatus(Order.Status.CANCELLED);
        order.setMemo("期限內未完成付款");
        orderRepository.save(order);
    }

    /**
     * 批次檢查並取消過期訂單
     * 可由排程任務定期呼叫
     */
    @Transactional
    public void checkAndCancelExpiredOrders() {
        log.info("[OrderService] 檢查過期訂單");

        List<Order> expiredOrders = orderRepository.findExpiredOrders(
                Order.Status.PENDING,
                LocalDateTime.now()
        );

        expiredOrders.forEach(this::cancelExpiredOrder);

        log.info("[OrderService] 已取消 {} 筆過期訂單", expiredOrders.size());
    }
}
