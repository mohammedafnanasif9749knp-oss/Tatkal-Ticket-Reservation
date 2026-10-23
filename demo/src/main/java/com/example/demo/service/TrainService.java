package com.example.demo.service;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.model.Train;
import com.example.demo.repository.TrainRepository;

@Service
public class TrainService {
    private final TrainRepository trainRepository;
    public TrainService(TrainRepository trainRepository)
    {
        this.trainRepository=trainRepository;
    }
public List<Train> getAllTrains(){
    return trainRepository.findAll();
}
public Train createTrain(Train newTrain){
return trainRepository.save(newTrain);
}
}
