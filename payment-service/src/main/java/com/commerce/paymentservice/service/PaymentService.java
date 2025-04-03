package com.commerce.paymentservice.service;

import com.commerce.paymentservice.entity.Payment;
import com.commerce.paymentservice.kafka.PaymentEventProducer;
import com.commerce.paymentservice.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer eventProducer;

    public Payment processPayment(String orderId, String userId, BigDecimal amount) {
        // 실제 결제 처리 대신 가상 결제 처리
        Payment payment = Payment.createPayment(orderId, userId, amount);



        // 5%의 확률로 결제 실패 시뮬레이션
        if (Math.random() < 0.05) {
            payment.failPayment();
            Payment savedPayment = paymentRepository.save(payment);

            // 결제 실패 이벤트 발행 - SAGA 패턴에 따른 보상 트랜잭션 트리거
            eventProducer.publishPaymentFailedEvent(savedPayment);

            return savedPayment;
        }

        // 가상 결제 처리 - 항상 성공한다고 가정
        payment.completePayment();


        Payment savedPayment = paymentRepository.save(payment);

        // 결제 완료 이벤트 발행
        eventProducer.publishPaymentCompletedEvent(savedPayment);

        return savedPayment;
    }

    public Payment cancelPayment(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new EntityNotFoundException("Payment not found for order: " + orderId));

        payment.cancelPayment();
        Payment savedPayment = paymentRepository.save(payment);

        // 결제 취소 이벤트 발행
        eventProducer.publishPaymentCancelledEvent(savedPayment);

        return savedPayment;
    }

}