package com.example.demo.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.model.route;
import com.example.demo.model.routestop;
import com.example.demo.model.station;
import com.example.demo.repository.routeRepository;
import com.example.demo.repository.routestopRepository;
import com.example.demo.repository.stationRepository;

@Service
public class routestopService {
    private final routestopRepository routeStopRepository;
    private final routeRepository routeRepository;
    private final stationRepository stationRepository;

    public routestopService(
            routestopRepository routeStopRepository,
            routeRepository routeRepository,
            stationRepository stationRepository) {
        this.routeStopRepository = routeStopRepository;
        this.routeRepository = routeRepository;
        this.stationRepository = stationRepository;
    }

    public List<routestop> getAllRouteStops() {
        return routeStopRepository.findAll();
    }

    public routestop createRouteStop(routestop newRouteStop) {
        if (newRouteStop == null
                || newRouteStop.getRoute() == null
                || newRouteStop.getRoute().getId() == null
                || newRouteStop.getStation() == null
                || newRouteStop.getStation().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Route and station ids are required");
        }

        route route = routeRepository.findById(newRouteStop.getRoute().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Route not found: " + newRouteStop.getRoute().getId()));
        station station = stationRepository.findById(newRouteStop.getStation().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Station not found: " + newRouteStop.getStation().getId()));

        newRouteStop.setRoute(route);
        newRouteStop.setStation(station);
        return routeStopRepository.save(newRouteStop);
    }
}
