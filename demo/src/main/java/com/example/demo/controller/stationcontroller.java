package com.example.demo.controller;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.stationRequestDTO;
import com.example.demo.model.station;
import com.example.demo.service.stationService;

import jakarta.validation.Valid;
@RestController
@RequestMapping("/api/stations")
public class stationcontroller {
    private final stationService stationservice;
    public stationcontroller(stationService stationservice)
    {
        this.stationservice=stationservice;
    }
    @GetMapping
    public ResponseEntity<List<station>> getAllStations(){
        return ResponseEntity.ok(stationservice.getAllStations());
    }
    @PostMapping
    public ResponseEntity<station> createstation(@Valid @RequestBody stationRequestDTO request)
    {
        station newstation=new station();
        newstation.setName(request.getName());
        newstation.setCode(request.getCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(stationservice.createStation(newstation));
    }
}
