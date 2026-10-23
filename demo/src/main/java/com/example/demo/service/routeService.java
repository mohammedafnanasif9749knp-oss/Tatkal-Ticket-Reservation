package com.example.demo.service;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.model.route;
import com.example.demo.repository.routeRepository;
@Service
public class routeService {
    private final routeRepository routeRepository;
public routeService(routeRepository routeRepository){
    this.routeRepository=routeRepository;
}
public List<route> getAllRoutes(){
    return routeRepository.findAll();
}
public route createRoute(route Route){
    return routeRepository.save(Route);
}
}
