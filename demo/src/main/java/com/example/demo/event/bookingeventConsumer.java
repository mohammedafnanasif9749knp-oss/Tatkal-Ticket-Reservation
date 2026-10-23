package com.example.demo.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class bookingeventConsumer {

    @KafkaListener(
        topics = "${app.kafka.booking-topic:booking-events}",
        groupId = "tatkal-booking-group"
    )
    public void consumeBookingEvent(bookingEvent event) {

        System.out.println("===== KAFKA EVENT RECEIVED =====");
        System.out.println("Event Type: " + event.getEventType());
        System.out.println("Booking ID: " + event.getBookingId());
        System.out.println("Journey ID: " + event.getJourneyId());
        System.out.println("Seat ID: " + event.getSeatId());
        System.out.println("================================");
    }
}
