package com.commerce.orderservice.repository;

import com.commerce.orderservice.dto.OrderListByUserDto;
import com.commerce.orderservice.dto.OrderProductListDto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepositoryDsl {

    Page<OrderListByUserDto> findOrderListByUser(String userId, Pageable pageable);

    List<OrderProductListDto> findOrderProductList(Long productId);
}