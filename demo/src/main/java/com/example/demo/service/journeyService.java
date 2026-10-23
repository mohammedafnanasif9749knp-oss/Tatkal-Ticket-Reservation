package com.example.demo.service;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.model.Train;
import com.example.demo.model.journey;
import com.example.demo.repository.TrainRepository;
import com.example.demo.repository.journeyrepository;

@Service
public class journeyService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final journeyrepository journeyRepository;
    private final TrainRepository trainRepository;

    public journeyService(journeyrepository journeyRepository,
                          TrainRepository trainRepository) {
        this.journeyRepository = journeyRepository;
        this.trainRepository = trainRepository;
    }
    public void clearCache() {
    redisTemplate.delete("message");
}
    // Redis test
    public String testRedis() {
        redisTemplate.opsForValue().set("test", "Hello Redis");
        return (String) redisTemplate.opsForValue().get("test");
    }
    public String cacheTest() {

    String key = "message";

    Object cached = redisTemplate.opsForValue().get(key);

    if (cached != null) {
        return "FROM REDIS: " + cached;
    }

    String data = "Hello from database";

    redisTemplate.opsForValue().set(key, data,10,TimeUnit.MINUTES);

    return "FROM DATABASE: " + data;
}
    public List<journey> getAllJourneys() {
        return journeyRepository.findAll();
    }

    public journey createJourney(journey newJourney) {

        if (newJourney == null ||
            newJourney.getTrain() == null ||
            newJourney.getTrain().getId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A train id is required");
        }

        Train train = trainRepository
                .findById(newJourney.getTrain().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Train not found: "
                        + newJourney.getTrain().getId()));

        newJourney.setTrain(train);

        return journeyRepository.save(newJourney);
    }
}
