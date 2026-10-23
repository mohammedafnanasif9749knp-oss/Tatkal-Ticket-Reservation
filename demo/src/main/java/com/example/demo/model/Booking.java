
package com.example.demo.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * A reservation for one seat on a journey.
 *
 * <p>Example request body (replace the ids with existing records):
 * <pre>{@code
 * {
 *   "user": { "id": 1 },
 *   "journey": { "id": 1 },
 *   "seat": { "id": 1 },
 *   "status": "PENDING"
 * }
 * }</pre>
 */
@Entity
@Table(name="bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private user user;
    @ManyToOne
    @JoinColumn(name = "journey_id", nullable = false)
    private journey journey;
    @ManyToOne
    @JoinColumn(name = "seat_id", nullable = false)
    private seat seat;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
private BookingStatus status;
    @Column(nullable = false)
    private LocalDateTime bookingTime;

    @PrePersist
    private void applyDefaults() {
        if (status == null) {
            status = BookingStatus.PENDING;
        }
        if (bookingTime == null) {
            bookingTime = LocalDateTime.now();
        }
    }

    public Integer getId() {
    return id;
}

public user getUser() {
    return user;
}

public journey getJourney() {
    return journey;
}

public seat getSeat() {
    return seat;
}

public BookingStatus getStatus() {
    return status;
}

public LocalDateTime getBookingTime() {
    return bookingTime;
}

public void setUser(user user) {
    this.user = user;
}

public void setJourney(journey journey) {
    this.journey = journey;
}

public void setSeat(seat seat) {
    this.seat = seat;
}

public void setStatus(BookingStatus status) {
    this.status = status;
}

public void setBookingTime(LocalDateTime bookingTime) {
    this.bookingTime = bookingTime;
}
}
