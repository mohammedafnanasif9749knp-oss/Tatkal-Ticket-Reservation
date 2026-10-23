package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.routestop;
import com.example.demo.service.routestopService;

@RestController
@RequestMapping("/api/route-stops")
public class routestopController {
    private final routestopService routeStopService;

    public routestopController(routestopService routeStopService) {
        this.routeStopService = routeStopService;
    }

    @GetMapping
    public ResponseEntity<List<routestop>> getAllRouteStops() {
        return ResponseEntity.ok(routeStopService.getAllRouteStops());
    }

    @PostMapping
    public ResponseEntity<routestop> createRouteStop(@RequestBody routestop newRouteStop) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(routeStopService.createRouteStop(newRouteStop));
    }
}
