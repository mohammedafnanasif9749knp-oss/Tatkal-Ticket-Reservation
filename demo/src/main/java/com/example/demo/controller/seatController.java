package com.example.demo.controller;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.seat;
import com.example.demo.service.seatService;
@RestController
@RequestMapping("/api/seats")
public class seatController {

    private final seatService seatService;

    public seatController(seatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping
    public ResponseEntity<List<seat>> getAllSeats() {
        return ResponseEntity.ok(seatService.getAllSeats());
    }

    @PostMapping
    public ResponseEntity<seat> createSeat(@RequestBody seat newSeat) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(seatService.createSeat(newSeat));
    }
}
