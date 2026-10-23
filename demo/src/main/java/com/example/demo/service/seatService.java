package com.example.demo.service;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.model.coach;
import com.example.demo.model.seat;
import com.example.demo.repository.coachRepository;
import com.example.demo.repository.seatRepository;

@Service
public class seatService {

    private final seatRepository seatrepository;
    private final coachRepository coachRepository;

    public seatService(seatRepository seatrepository, coachRepository coachRepository) {
        this.seatrepository = seatrepository;
        this.coachRepository = coachRepository;
    }

    public List<seat> getAllSeats() {
        return seatrepository.findAll();
    }

    public seat createSeat(seat newSeat) {
        if (newSeat == null || newSeat.getCoach() == null || newSeat.getCoach().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A coach id is required");
        }
        coach coach = coachRepository.findById(newSeat.getCoach().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Coach not found: " + newSeat.getCoach().getId()));
        newSeat.setCoach(coach);
        return seatrepository.save(newSeat);
    }
}
