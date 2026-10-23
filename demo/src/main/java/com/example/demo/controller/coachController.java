package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.coach;
import com.example.demo.service.coachService;

@RestController
@RequestMapping("/api/coaches")
public class coachController {
    private final coachService coachservice;
    public coachController(coachService coachservice)
    {
        this.coachservice=coachservice;
    }
@GetMapping
public ResponseEntity<List<coach>> getAllcoaches(){
    return ResponseEntity.ok(coachservice.getAllCoaches());
}
@PostMapping
public ResponseEntity<coach> createCoach(@RequestBody coach newcoach)
{
    return ResponseEntity.status(HttpStatus.CREATED).body(coachservice.createCoach(newcoach));
}
}
