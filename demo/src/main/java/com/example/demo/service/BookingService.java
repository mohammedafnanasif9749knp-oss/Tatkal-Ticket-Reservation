package com.example.demo.service;

import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.bookingRequest;
import com.example.demo.event.bookingEvent;
import com.example.demo.exception.BookingNotfoundException;
import com.example.demo.exception.JourneyNotFoundException;
import com.example.demo.exception.SeatNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.model.Booking;
import com.example.demo.model.BookingStatus;
import com.example.demo.model.journey;
import com.example.demo.model.seat;
import com.example.demo.model.user;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.journeyrepository;
import com.example.demo.repository.seatRepository;
import com.example.demo.repository.userRepository;

@Service
public class BookingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingrepository;
    private final userRepository userrepository;
    private final journeyrepository journeyrepository;
    private final seatRepository seatrepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ApplicationEventPublisher eventPublisher;

    public BookingService(
            BookingRepository bookingrepository,
            userRepository userrepository,
            journeyrepository journeyrepository,
            seatRepository seatrepository,
            RedisTemplate<String, Object> redisTemplate,
            ApplicationEventPublisher eventPublisher) {

        this.bookingrepository = bookingrepository;
        this.userrepository = userrepository;
        this.journeyrepository = journeyrepository;
        this.seatrepository = seatrepository;
        this.redisTemplate = redisTemplate;
        this.eventPublisher = eventPublisher;
    }

    public List<Booking> getAllBookings() {
        return bookingrepository.findAll();
    }

    // Check seat availability using Redis first
    public boolean isSeatAvialable(Integer journeyId, Integer seatId) {

        String key = "availability:" + journeyId + ":" + seatId;

        // 1. Check Redis
        Object cached = null;
        try {
            cached = redisTemplate.opsForValue().get(key);
        } catch (DataAccessException error) {
            LOGGER.warn("Availability cache read failed; checking the database", error);
        }

        if (cached != null) {
            return Boolean.parseBoolean(cached.toString());
        }

        // 2. Check PostgreSQL
        boolean alreadybooked =
                bookingrepository.existsByJourney_IdAndSeat_IdAndStatusIn(
                        journeyId,
                        seatId,
                        List.of(
                                BookingStatus.PENDING,
                                BookingStatus.CONFIRMED
                        )
                );

        boolean available = !alreadybooked;

        // 3. Save result in Redis
        try {
            redisTemplate.opsForValue().set(key, String.valueOf(available));
        } catch (DataAccessException error) {
            LOGGER.warn("Availability cache write failed", error);
        }

        return available;
    }

    public bookingRequest suggestAvailableSeat(bookingRequest request) {
        if (request == null
                || request.getUserId() == null
                || request.getJourneyId() == null
                || request.getSeatId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "userId, journeyId, and seatId are required");
        }

        user user = userrepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        journey journey = journeyrepository.findById(request.getJourneyId())
                .orElseThrow(() -> new JourneyNotFoundException("Journey not found"));
        seat requestedSeat = seatrepository.findById(request.getSeatId())
                .orElseThrow(() -> new SeatNotFoundException("Seat not found"));

        if (!journey.getTrain().getId().equals(requestedSeat.getCoach().getTrain().getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Seat does not belong to the journey's train");
        }

        boolean requestedSeatIsBooked =
                bookingrepository.existsByJourney_IdAndSeat_IdAndStatusIn(
                        request.getJourneyId(),
                        request.getSeatId(),
                        List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED));

        Integer availableSeatId = request.getSeatId();
        if (requestedSeatIsBooked) {
            availableSeatId = seatrepository
                    .findAllByCoach_Train_IdOrderByIdAsc(journey.getTrain().getId())
                    .stream()
                    .filter(candidate -> !bookingrepository
                            .existsByJourney_IdAndSeat_IdAndStatusIn(
                                    journey.getId(),
                                    candidate.getId(),
                                    List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)))
                    .map(seat -> seat.getId())
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "No available seats for this journey"));
        }

        bookingRequest suggestedRequest = new bookingRequest();
        suggestedRequest.setUserId(user.getId());
        suggestedRequest.setJourneyId(journey.getId());
        suggestedRequest.setSeatId(availableSeatId);
        return suggestedRequest;
    }

    @Transactional
    public Booking cancelBooking(Integer bookingId) {

        Booking booking = bookingrepository.findById(bookingId)
                .orElseThrow(() ->
                        new BookingNotfoundException("Booking Not found"));

        booking.setStatus(BookingStatus.CANCELLED);

        Booking saved = bookingrepository.save(booking);
        publishBookingEvent("BOOKING_CANCELLED", saved);

        return saved;
    }

    @Transactional
    public Booking createBooking(bookingRequest request) {

        if (request == null || request.getUserId() == null
                || request.getJourneyId() == null || request.getSeatId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "userId, journeyId, and seatId are required");
        }

        user user = userrepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        journey journey = journeyrepository.findById(request.getJourneyId())
                .orElseThrow(() ->
                        new JourneyNotFoundException("Journey not found"));

        seat seat = seatrepository.findById(request.getSeatId())
                .orElseThrow(() ->
                        new SeatNotFoundException("Seat not found"));

        if (!journey.getTrain().getId().equals(seat.getCoach().getTrain().getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Seat does not belong to the journey's train");
        }

        boolean alreadyBooked =
                bookingrepository.existsByJourney_IdAndSeat_IdAndStatusIn(
                        request.getJourneyId(),
                        request.getSeatId(),
                        List.of(
                                BookingStatus.PENDING,
                                BookingStatus.CONFIRMED
                        )
                );

        if (alreadyBooked) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Seat is already booked for this journey");
        }

        Booking booking = new Booking();

        booking.setUser(user);
        booking.setJourney(journey);
        booking.setSeat(seat);

        // Save booking in PostgreSQL
        Booking saved = bookingrepository.save(booking);
        publishBookingEvent("BOOKING_CREATED", saved);

        return saved;
    }

    private void publishBookingEvent(String eventType, Booking booking) {
        eventPublisher.publishEvent(new bookingEvent(
                eventType,
                booking.getId(),
                booking.getJourney().getId(),
                booking.getSeat().getId()));
    }
}
