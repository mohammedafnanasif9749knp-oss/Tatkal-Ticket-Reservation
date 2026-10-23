package com.example.demo.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import java.util.Optional;
import java.util.List;
import com.example.demo.model.seat;

import jakarta.persistence.LockModeType;
public interface seatRepository extends JpaRepository<seat,Integer>{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<seat> findById(Integer id);

    List<seat> findAllByCoach_Train_IdOrderByIdAsc(Integer trainId);
}
