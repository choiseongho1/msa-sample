package com.commerce.orderservice.repository;

import static com.commerce.orderservice.entity.QOrder.order;
import static com.commerce.orderservice.entity.QOrderProduct.orderProduct;

import com.commerce.orderservice.dto.OrderListByUserDto;
import com.commerce.orderservice.dto.OrderProductListDto;
import com.commerce.orderservice.dto.QOrderListByUserDto;
import com.commerce.orderservice.dto.QOrderProductListDto;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;


@RequiredArgsConstructor
public class OrderRepositoryDslImpl implements OrderRepositoryDsl {
     private final JPAQueryFactory queryFactory;



    @Override
    public Page<OrderListByUserDto> findOrderListByUser(String userId, Pageable pageable) {
        final Long totalCount = getCountQuery(userId)
                .fetchOne();

        final List<OrderListByUserDto> orderListByUser = getListQuery(userId)
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        return new PageImpl<OrderListByUserDto>(orderListByUser, pageable, (totalCount==null)?0:totalCount);
    }


    private JPAQuery<Long> getCountQuery(String userId ) {
        return queryFactory.select(order.count())
                .from(order)
                .where(order.userId.eq(userId));
    }


    private JPAQuery<OrderListByUserDto> getListQuery(String userId) {
        return queryFactory
                .select(new QOrderListByUserDto(
                        order.id,
                        order.orderDate,
                        order.orderStatus,
                        order.orderShippingFee,
                        order.orderPrice,
                        order.orderCardCompany
                ))
                .from(order)
                .where(order.userId.eq(userId))
                .orderBy(order.id.desc());
    }

    @Override
    public List<OrderProductListDto> findOrderProductList(Long orderId) {
        return queryFactory
            .select(new QOrderProductListDto(
                orderProduct.productId,
                orderProduct.orderProductQuantity,
                orderProduct.orderProductPrice,
                orderProduct.orderProductOption,
                orderProduct.orderProductOptionPrice
            ))
            .from(orderProduct)
            .where(orderProduct.order.id.eq(orderId))
            .orderBy(orderProduct.id.desc())
            .fetch();
    }

}