package com.example.demo.controller;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.journey;
import com.example.demo.service.journeyService;
@RestController
@RequestMapping("/api/journeys")
public class journeyController {

    private final journeyService journeyService;

    public journeyController(journeyService journeyService) {
        this.journeyService = journeyService;
    }

    @GetMapping
    public ResponseEntity<List<journey>> getAllJourneys() {
        return ResponseEntity.ok(journeyService.getAllJourneys());
    }
   @GetMapping("/redis-test")
public String redisTest() {
    return journeyService.testRedis();
}
@GetMapping("/cache-test")
public String cacheTest() {
    return journeyService.cacheTest();
}
@DeleteMapping("/clear-cache")
public String clearCache() {
    journeyService.clearCache();
    return "Cache cleared";
}
    @PostMapping
    public ResponseEntity<journey> createJourney(
            @RequestBody journey newJourney) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(journeyService.createJourney(newJourney));
    }
}
