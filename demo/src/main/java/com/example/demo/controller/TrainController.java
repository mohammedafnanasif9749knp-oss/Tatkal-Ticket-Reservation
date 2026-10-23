package com.example.demo.controller;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Train;
import com.example.demo.service.TrainService;
@RestController
@RequestMapping("/api/trains")
public class TrainController {
    private final TrainService trainService;

    public TrainController(TrainService trainService) {
        this.trainService = trainService;
    }
@GetMapping
public ResponseEntity<List<Train>> getAllTrains(){
    return ResponseEntity.ok(trainService.getAllTrains());
}
@PostMapping
public ResponseEntity<Train> createTrain(@RequestBody Train newTrain)
{
    return ResponseEntity.status(HttpStatus.CREATED).body(trainService.createTrain(newTrain));
}
}
