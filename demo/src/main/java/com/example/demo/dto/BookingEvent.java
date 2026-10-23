package com.example.demo.dto;

import java.time.Instant;

public record BookingEvent(
        Integer bookingId,
        String status,
        Integer userId,
        Integer journeyId,
        Integer seatId,
        Instant occurredAt) {
}
