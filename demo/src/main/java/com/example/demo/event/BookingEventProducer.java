package com.example.demo.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class BookingEventProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookingEventProducer.class);

    private final KafkaTemplate<String, bookingEvent> kafkaTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final String topic;

    public BookingEventProducer(
            KafkaTemplate<String, bookingEvent> kafkaTemplate,
            RedisTemplate<String, Object> redisTemplate,
            @Value("${app.kafka.booking-topic:booking-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.redisTemplate = redisTemplate;
        this.topic = topic;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishBookingEvent(bookingEvent event) {
        String availabilityKey = "availability:" + event.getJourneyId() + ":" + event.getSeatId();
        try {
            redisTemplate.delete(availabilityKey);
        } catch (DataAccessException error) {
            LOGGER.error("Failed to invalidate seat availability cache for booking {}",
                    event.getBookingId(), error);
        }

        try {
            kafkaTemplate.send(topic, event.getBookingId().toString(), event)
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            LOGGER.error("Failed to publish booking event for booking {}",
                                    event.getBookingId(), error);
                        } else {
                            LOGGER.info("Published booking event: type={}, bookingId={}, partition={}, offset={}",
                                    event.getEventType(),
                                    event.getBookingId(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        }
                    });
        } catch (KafkaException error) {
            LOGGER.error("Failed to publish booking event for booking {}", event.getBookingId(), error);
        }
    }
}
