package com.example.demo.service;
import org.springframework.stereotype.*;
import com.example.demo.repository.coachRepository;
import com.example.demo.repository.TrainRepository;
import com.example.demo.model.coach;
import com.example.demo.model.Train;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
@Service
public class coachService {
    private final coachRepository coachrepository;
    private final TrainRepository trainRepository;

    public coachService(coachRepository coachrepository, TrainRepository trainRepository) {
        this.coachrepository=coachrepository;
        this.trainRepository = trainRepository;
    }
    public List<coach> getAllCoaches(){
        return coachrepository.findAll();
    }
    public coach createCoach(coach newCoach){
        if (newCoach == null || newCoach.getTrain() == null || newCoach.getTrain().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A train id is required");
        }
        Train train = trainRepository.findById(newCoach.getTrain().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Train not found: " + newCoach.getTrain().getId()));
        newCoach.setTrain(train);
        return coachrepository.save(newCoach);
    }
}
