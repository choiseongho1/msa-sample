package com.commerce.orderservice.service;

import com.commerce.common.exception.CustomException;
import com.commerce.orderservice.dto.OrderListByUserDto;
import com.commerce.orderservice.dto.OrderProductListDto;
import com.commerce.orderservice.dto.OrderProductSaveDto;
import com.commerce.orderservice.dto.OrderSaveDto;
import com.commerce.orderservice.dto.ProductOptionDto;
import com.commerce.orderservice.dto.ProductPriceDto;
import com.commerce.orderservice.entity.Order;
import com.commerce.orderservice.entity.OrderProduct;
import com.commerce.orderservice.feign.ImageServiceClient;
import com.commerce.orderservice.feign.ProductServiceClient;
import com.commerce.orderservice.feign.UserServiceClient;
import com.commerce.orderservice.kafka.OrderEventProducer;
import com.commerce.orderservice.kafka.ProductStockProducer;
import com.commerce.orderservice.repository.OrderProductRepository;
import com.commerce.orderservice.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 상품 주문 Service
 *
 * @author : choiseongho
 * @fileName : OrderService.java
 * @since : 2025-01-09
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;


    private final UserServiceClient userServiceClient;
    private final ProductServiceClient productServiceClient;
    private final ImageServiceClient imageServiceClient;


    private final ProductStockProducer productStockProducer;
    private final OrderEventProducer orderEventProducer;

    /**
     * 주문정보 저장 service
     *
     * @param orderSaveDto the order save dto
     */
    @Transactional
    public Order saveOrder(OrderSaveDto orderSaveDto) {

        int totalOrderAmount = 0; // 전체 주문 금액

        for (OrderProductSaveDto dto : orderSaveDto.getOrderProductSaveList()){
            // 상품 검증
            boolean isValidProduct = productServiceClient.validateProduct(dto.getProductId());
            if (!isValidProduct) {
                log.error("Invalid Product ID: {}", dto.getProductId());
                throw new CustomException("유효하지 않은 상품입니다.");
            }


            // 상품 가격 검증
            ProductPriceDto productPrice = productServiceClient.getProductPrice(dto.getProductId());
            if (!productPrice.getPrice().equals(dto.getOrderProductPrice())) {
                log.error("Product price mismatch for Product ID: {}", dto.getProductId());
                throw new CustomException("상품 가격이 일치하지 않습니다.");
            }

            // 상품 옵션 검증
            if (dto.getOrderProductOption() != null) {
                ProductOptionDto optionDto = productServiceClient.getProductOption(
                    dto.getProductId(),
                    dto.getOrderProductOption()
                );
                if (!optionDto.getPrice().equals(dto.getOrderProductOptionPrice())) {
                    log.error("Product option price mismatch for Product ID: {} and Option: {}",
                        dto.getProductId(), dto.getOrderProductOption());
                    throw new CustomException("상품 옵션 가격이 일치하지 않습니다.");
                }
            }

            // 상품별 총 구매 금액 계산 (상품 가격 + 옵션 가격 * 수량)
            int productTotalPrice = (dto.getOrderProductPrice() +
                (dto.getOrderProductOptionPrice() != null ? dto.getOrderProductOptionPrice() : 0))
                * dto.getOrderProductQuantity();

            totalOrderAmount += productTotalPrice;

        }

        // 전체 주문 금액 설정 (배송비 포함)
        int totalAmountWithShipping = totalOrderAmount + orderSaveDto.getOrderShippingFee();
        orderSaveDto.setOrderPrice(totalAmountWithShipping);

        // 주문정보 저장
        Order order = orderSaveDto.toEntity();
        Order saveOrder = orderRepository.save(order);

        // 상품 옵션 저장
        List<OrderProduct> orderProducts = orderSaveDto.getOrderProductSaveList().stream()
            .map(orderProductDto -> {
                OrderProduct orderProduct = orderProductDto.toEntity();
                orderProduct.setOrder(saveOrder);
                return orderProduct;
            })
            .toList();

        orderProductRepository.saveAll(orderProducts);


        return saveOrder;
    }



    @Transactional
    public Order cancelOrder(Long orderId) {
        // 주문 조회
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new CustomException("주문을 찾을 수 없습니다."));

        // 주문 상태 확인 - 결제 완료 상태일 때만 취소 가능
        if (order.getOrderStatus() != Order.OrderStatus.PAYMENT_COMPLETED &&
            order.getOrderStatus() != Order.OrderStatus.WAITING_FOR_PAYMENT) {
            throw new CustomException("취소할 수 없는 주문 상태입니다: " + order.getOrderStatus());
        }

        // 주문 상태를 취소로 변경
        order.setOrderStatus(Order.OrderStatus.CANCELED);
        order.setOrderUpdatedAt(LocalDateTime.now());

        // 주문 취소 이벤트 발행 - 결제 서비스에 취소 요청
        if (order.getOrderStatus() == Order.OrderStatus.PAYMENT_COMPLETED) {
            orderEventProducer.publishOrderCancelledEvent(order.getId());
        }

        // 재고 복구 이벤트 발행
        restoreProductStock(order);

        return orderRepository.save(order);
    }

    /**
     * 주문 취소 시 상품 재고 복구
     *
     * @param order 취소할 주문 정보
     */
    private void restoreProductStock(Order order) {
        List<OrderProduct> orderProducts = orderProductRepository.findByOrderId(order.getId());

        log.info("주문 취소에 따른 재고 복구 시작 - 주문 ID: {}, 상품 수: {}", order.getId(), orderProducts.size());

        for (OrderProduct orderProduct : orderProducts) {
            try {
                productStockProducer.increaseStock(
                    orderProduct.getProductId(),
                    orderProduct.getOrderProductOption(),
                    orderProduct.getOrderProductQuantity(),
                    order.getId()
                );

                log.info("재고 복구 이벤트 발행 - 상품 ID: {}, 옵션 ID: {}, 수량: {}",
                    orderProduct.getProductId(),
                    orderProduct.getOrderProductOption(),
                    orderProduct.getOrderProductQuantity());
            } catch (Exception e) {
                log.error("재고 복구 이벤트 발행 실패 - 상품 ID: {}, 옵션 ID: {}",
                    orderProduct.getProductId(), orderProduct.getOrderProductOption(), e);
                throw new RuntimeException("재고 복구 처리 중 오류가 발생했습니다.", e);
            }
        }
    }





    @Transactional
    public void processOrder(OrderSaveDto orderSaveDto) {
        // 사용자 검증
        boolean isValidUser = userServiceClient.validateUser(orderSaveDto.getUserId());
        if (!isValidUser) {
            throw new CustomException("유효하지 않은 사용자입니다.");
        }

        // 주문 저장
        Order savedOrder = saveOrder(orderSaveDto);

        // 상품 재고 감소 이벤트 발행
        decreaseProductStock(savedOrder, orderSaveDto);

        // 주문 생성 이벤트 발행
        orderEventProducer.publishOrderCreatedEvent(
            savedOrder.getId(),
            savedOrder.getUserId(),
            savedOrder.getOrderPrice()
        );
    }

    /**
     * 주문 생성 시 상품 재고 감소
     *
     * @param order 저장된 주문 정보
     * @param orderSaveDto 주문 저장 DTO
     */
    private void decreaseProductStock(Order order, OrderSaveDto orderSaveDto) {
        log.info("주문에 따른 재고 감소 시작 - 주문 ID: {}, 상품 수: {}",
            order.getId(), orderSaveDto.getOrderProductSaveList().size());

        for (OrderProductSaveDto productDto : orderSaveDto.getOrderProductSaveList()) {
            try {
                productStockProducer.decreaseStock(
                    productDto.getProductId(),
                    productDto.getOrderProductOption(),
                    productDto.getOrderProductQuantity(),
                    order.getId()
                );

                log.info("재고 감소 이벤트 발행 - 상품 ID: {}, 옵션 ID: {}, 수량: {}",
                    productDto.getProductId(),
                    productDto.getOrderProductOption(),
                    productDto.getOrderProductQuantity());
            } catch (Exception e) {
                log.error("재고 감소 이벤트 발행 실패 - 상품 ID: {}, 옵션 ID: {}",
                    productDto.getProductId(), productDto.getOrderProductOption(), e);
                throw new RuntimeException("재고 감소 처리 중 오류가 발생했습니다.", e);
            }
        }
    }

    /**
     * 사용자별 주문 정보 조회 service
     *
     * @param userId   the user id
     * @param pageable the pageable
     */
    public Page<OrderListByUserDto> findOrderListByUserId(String userId, Pageable pageable) {
        return orderRepository.findOrderListByUser(userId, pageable);
    }

    /**
     * 주문 정보 별 주문 상품 목록 조회 Service
     *
     * @param orderId the order id
     */
    @Transactional(readOnly = true)
    @CircuitBreaker(name = "imageService", fallbackMethod = "fallbackOrderProductList")
    public List<OrderProductListDto> findOrderProductList(Long orderId) {

        return orderRepository.findOrderProductList(orderId)
            .stream()
            .peek(orderProductListDto -> {
                Long imageId = productServiceClient.findProductImageId(orderProductListDto.getProductId());

                // imageId가 존재하는 경우에만 image정보 가져옴
                if(!Objects.isNull(imageId)){
                    try {
                        // Feign Client 로 imageLob정보 조회
                        String imageLob = imageServiceClient.getImageLob(imageId);
                        orderProductListDto.setImageLob(imageLob);
                    } catch (Exception e) {
                        log.error("이미지 서비스 호출 중 오류 발생: {}", e.getMessage(), e);
                        throw e; // Circuit Breaker가 이 예외를 감지하도록 다시 던짐
                    }
                }
            })
            .toList();
    }


    /**
     * 주문 상태 업데이트
     *
     * @param orderId the order id
     */
    @Transactional
    public void updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new CustomException("주문을 찾을 수 없습니다."));

        order.setOrderStatus(status);
        order.setOrderUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    // Fallback 시 이미지 정보 없이 return
    public List<OrderProductListDto> fallbackOrderProductList(Long orderId, Throwable throwable) {
        log.warn("이미지 서비스 호출 실패로 fallback 실행: {}", throwable.getMessage());

        // 기본 주문 상품 목록 조회 (이미지 정보 없이)
        List<OrderProductListDto> orderProducts = orderRepository.findOrderProductList(orderId);

        // 이미지 정보를 null로 설정
        orderProducts.forEach(product -> product.setImageLob(null));

        return orderProducts;
    }
}
