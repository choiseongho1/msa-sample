package com.commerce.productservice.config;

import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class KafkaAcknowledgmentManager {

    private static final ThreadLocal<Acknowledgment> currentAcknowledgment = new ThreadLocal<>();

    public void setCurrentAcknowledgment(Acknowledgment acknowledgment) {
        currentAcknowledgment.set(acknowledgment);
    }

    public Acknowledgment getCurrentAcknowledgment() {
        return currentAcknowledgment.get();
    }

    public void acknowledge() {
        Acknowledgment ack = currentAcknowledgment.get();
        if (ack != null) {
            ack.acknowledge();
            currentAcknowledgment.remove(); // Clean up after use
        }
    }

    public void clear() {
        currentAcknowledgment.remove();
    }
}