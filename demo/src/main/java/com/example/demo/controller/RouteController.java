package com.example.demo.controller;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.route;
import com.example.demo.service.routeService;
@RestController
@RequestMapping("/api/routes")
public class RouteController {
    private final routeService routeService;
    public RouteController(routeService routeService){
        this.routeService=routeService;
    }
    @GetMapping
public List<route> getAllRoutes(){
return routeService.getAllRoutes();
}
@PostMapping
public ResponseEntity<route> createRoute(@RequestBody route newRoute){
    return ResponseEntity.status(HttpStatus.CREATED).body(routeService.createRoute(newRoute));
}
}
