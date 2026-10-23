package com.example.demo.controller;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.bookingRaceData;
import com.example.demo.model.coach;
import com.example.demo.model.journey;
import com.example.demo.model.seat;
import com.example.demo.model.user;
import com.example.demo.repository.coachRepository;
import com.example.demo.repository.journeyrepository;
import com.example.demo.repository.seatRepository;
import com.example.demo.repository.userRepository;

@RestController
@RequestMapping("/api/demo")
@ConditionalOnProperty(prefix = "app.demo-data", name = "enabled", havingValue = "true")
public class DemoLoadTestController {
    private static final String DEMO_EMAIL = "demo.booking@example.test";
    private static final String DEMO_TRAIN_NUMBER = "DEMO-1001";
    private static final String DEMO_COACH_NUMBER = "DEMO-C1";
    private static final String DEMO_RACE_SEAT_NUMBER = "DEMO-RACE-1";

    private final userRepository userRepository;
    private final journeyrepository journeyRepository;
    private final seatRepository seatRepository;
    private final coachRepository coachRepository;

    public DemoLoadTestController(
            userRepository userRepository,
            journeyrepository journeyRepository,
            seatRepository seatRepository,
            coachRepository coachRepository) {
        this.userRepository = userRepository;
        this.journeyRepository = journeyRepository;
        this.seatRepository = seatRepository;
        this.coachRepository = coachRepository;
    }

    @GetMapping("/booking-race-data")
    public bookingRaceData getBookingRaceData() {
        user demoUser = userRepository.findAll().stream()
                .filter(existing -> DEMO_EMAIL.equals(existing.getEmail()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Demo user not found"));
        journey demoJourney = journeyRepository.findAll().stream()
                .filter(existing -> DEMO_TRAIN_NUMBER.equals(existing.getTrain().getTrainNumber()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Demo journey not found"));
        seat raceSeat = seatRepository.findAll().stream()
                .filter(existing -> DEMO_RACE_SEAT_NUMBER.equals(existing.getSeatNumber())
                        && "DEMO-C1".equals(existing.getCoach().getCoachNumber()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Demo race seat not found"));

        return new bookingRaceData(demoUser.getId(), demoJourney.getId(), raceSeat.getId());
    }

    @PostMapping("/booking-data")
    @ResponseStatus(HttpStatus.CREATED)
    public bookingRaceData createBookingData() {
        user demoUser = userRepository.findAll().stream()
                .filter(existing -> DEMO_EMAIL.equals(existing.getEmail()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Demo user not found"));
        journey demoJourney = journeyRepository.findAll().stream()
                .filter(existing -> DEMO_TRAIN_NUMBER.equals(existing.getTrain().getTrainNumber()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Demo journey not found"));
        coach demoCoach = coachRepository.findAll().stream()
                .filter(existing -> DEMO_COACH_NUMBER.equals(existing.getCoachNumber())
                        && DEMO_TRAIN_NUMBER.equals(existing.getTrain().getTrainNumber()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Demo coach not found"));

        seat availableSeat = new seat();
        availableSeat.setCoach(demoCoach);
        availableSeat.setSeatNumber("TEST-" + UUID.randomUUID());
        availableSeat.setSeatType("WINDOW");
        seat savedSeat = seatRepository.save(availableSeat);

        return new bookingRaceData(demoUser.getId(), demoJourney.getId(), savedSeat.getId());
    }
}
