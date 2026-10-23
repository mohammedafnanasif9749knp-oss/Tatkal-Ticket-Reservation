package com.example.demo.service;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.model.station;
import com.example.demo.repository.stationRepository;
@Service
public class stationService {
    private final stationRepository stationrepository;
public stationService(stationRepository stationrepository)
{
    this.stationrepository=stationrepository;
}
public List<station> getAllStations(){
    return stationrepository.findAll();
}
public station createStation(station newStation)
{
return stationrepository.save(newStation);
}
}
