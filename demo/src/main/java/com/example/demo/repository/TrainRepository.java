package com.example.demo.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Train;
public interface TrainRepository extends JpaRepository<Train,Integer>{

}
