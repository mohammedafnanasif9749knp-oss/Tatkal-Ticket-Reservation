package com.example.demo.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.model.Booking;
import com.example.demo.model.BookingStatus;
import com.example.demo.model.Train;
import com.example.demo.model.coach;
import com.example.demo.model.journey;
import com.example.demo.model.seat;
import com.example.demo.model.user;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.TrainRepository;
import com.example.demo.repository.coachRepository;
import com.example.demo.repository.journeyrepository;
import com.example.demo.repository.seatRepository;
import com.example.demo.repository.userRepository;

@Component
@ConditionalOnProperty(prefix = "app.demo-data", name = "enabled", havingValue = "true")
public class DemoDataInitializer implements CommandLineRunner {
    private static final String DEMO_EMAIL = "demo.booking@example.test";
    private static final String DEMO_TRAIN_NUMBER = "DEMO-1001";
    private static final String DEMO_COACH_NUMBER = "DEMO-C1";
    private static final String DEMO_RACE_SEAT_NUMBER = "DEMO-RACE-1";
    private static final int DEMO_USER_COUNT = 10;
    private static final int DEMO_SEAT_COUNT = 100;
    private static final int DEMO_BOOKING_COUNT = 10;

    private final BookingRepository bookingRepository;
    private final userRepository userRepository;
    private final TrainRepository trainRepository;
    private final coachRepository coachRepository;
    private final journeyrepository journeyRepository;
    private final seatRepository seatRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataInitializer(
            BookingRepository bookingRepository,
            userRepository userRepository,
            TrainRepository trainRepository,
            coachRepository coachRepository,
            journeyrepository journeyRepository,
            seatRepository seatRepository,
            PasswordEncoder passwordEncoder) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.trainRepository = trainRepository;
        this.coachRepository = coachRepository;
        this.journeyRepository = journeyRepository;
        this.seatRepository = seatRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        List<user> demoUsers = createDemoUsers();
        Train demoTrain = trainRepository.findAll().stream()
                .filter(existing -> DEMO_TRAIN_NUMBER.equals(existing.getTrainNumber()))
                .findFirst()
                .orElseGet(this::createDemoTrain);

        coach demoCoach = coachRepository.findAll().stream()
                .filter(existing -> demoTrain.getId().equals(existing.getTrain().getId())
                        && DEMO_COACH_NUMBER.equals(existing.getCoachNumber()))
                .findFirst()
                .orElseGet(() -> createDemoCoach(demoTrain));
        LocalDate demoJourneyDate = LocalDate.now().plusDays(1);
        journey demoJourney = journeyRepository.findAll().stream()
                .filter(existing -> demoTrain.getId().equals(existing.getTrain().getId())
                        && demoJourneyDate.equals(existing.getJourneyDate()))
                .findFirst()
                .orElseGet(() -> createDemoJourney(demoTrain, demoJourneyDate));
        List<seat> demoSeats = createDemoSeats(demoCoach);
        seat raceSeat = findOrCreateSeat(demoCoach, DEMO_RACE_SEAT_NUMBER);
        resetRaceSeat(demoJourney, raceSeat);

        for (int i = 0; i < DEMO_BOOKING_COUNT; i++) {
            seat demoSeat = demoSeats.get(i);
            if (!bookingRepository.existsByJourney_IdAndSeat_Id(demoJourney.getId(), demoSeat.getId())) {
                Booking demoBooking = new Booking();
                demoBooking.setUser(demoUsers.get(i % demoUsers.size()));
                demoBooking.setJourney(demoJourney);
                demoBooking.setSeat(demoSeat);
                demoBooking.setStatus(BookingStatus.PENDING);
                demoBooking.setBookingTime(LocalDateTime.now());
                bookingRepository.save(demoBooking);
            }
        }
    }

    private List<user> createDemoUsers() {
        List<user> existingUsers = userRepository.findAll();
        List<user> demoUsers = new ArrayList<>();

        for (int i = 0; i < DEMO_USER_COUNT; i++) {
            int userIndex = i;
            String email = userIndex == 0 ? DEMO_EMAIL
                    : "demo.loadtest.user%03d@example.test".formatted(userIndex);
            user demoUser = existingUsers.stream()
                    .filter(existing -> email.equals(existing.getEmail()))
                    .findFirst()
                    .orElseGet(() -> createDemoUser(userIndex, email));
            demoUsers.add(demoUser);
        }

        return demoUsers;
    }

    private user createDemoUser(int index, String email) {
        user demoUser = new user();
        demoUser.setName(index == 0 ? "Demo Booking User" : "Demo Load Test User " + index);
        demoUser.setEmail(email);
        demoUser.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
        demoUser.setRole("USER");
        return userRepository.save(demoUser);
    }

    private Train createDemoTrain() {
        Train demoTrain = new Train();
        demoTrain.setTrainNumber(DEMO_TRAIN_NUMBER);
        demoTrain.setName("Demo Express");
        return trainRepository.save(demoTrain);
    }

    private coach createDemoCoach(Train train) {
        coach demoCoach = new coach();
        demoCoach.setTrain(train);
        demoCoach.setCoachNumber("DEMO-C1");
        demoCoach.setCoachType("AC");
        return coachRepository.save(demoCoach);
    }

    private journey createDemoJourney(Train train, LocalDate date) {
        journey demoJourney = new journey();
        demoJourney.setTrain(train);
        demoJourney.setJourneyDate(date);
        return journeyRepository.save(demoJourney);
    }

    private List<seat> createDemoSeats(coach coach) {
        List<seat> existingSeats = seatRepository.findAll();
        List<seat> demoSeats = new ArrayList<>();

        for (int i = 1; i <= DEMO_SEAT_COUNT; i++) {
            demoSeats.add(findOrCreateSeat(coach, "DEMO-" + i, existingSeats));
        }

        return demoSeats;
    }

    private seat findOrCreateSeat(coach coach, String seatNumber) {
        return findOrCreateSeat(coach, seatNumber, seatRepository.findAll());
    }

    private seat findOrCreateSeat(coach coach, String seatNumber, List<seat> existingSeats) {
        return existingSeats.stream()
                .filter(existing -> coach.getId().equals(existing.getCoach().getId())
                        && seatNumber.equals(existing.getSeatNumber()))
                .findFirst()
                .orElseGet(() -> createDemoSeat(coach, seatNumber));
    }

    private void resetRaceSeat(journey journey, seat raceSeat) {
        List<Booking> existingRaceBookings = bookingRepository
                .findAllByJourney_IdAndSeat_Id(journey.getId(), raceSeat.getId());
        existingRaceBookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.PENDING
                        || booking.getStatus() == BookingStatus.CONFIRMED)
                .forEach(booking -> booking.setStatus(BookingStatus.CANCELLED));
        bookingRepository.saveAll(existingRaceBookings);
    }

    private seat createDemoSeat(coach coach, String seatNumber) {
        seat demoSeat = new seat();
        demoSeat.setCoach(coach);
        demoSeat.setSeatNumber(seatNumber);
        demoSeat.setSeatType("WINDOW");
        return seatRepository.save(demoSeat);
    }
}
