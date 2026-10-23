package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Booking;
import com.example.demo.model.BookingStatus;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    boolean existsByJourney_IdAndSeat_Id(Integer journeyId, Integer seatId);

    List<Booking> findAllByJourney_IdAndSeat_Id(Integer journeyId, Integer seatId);

    boolean existsByJourney_IdAndSeat_IdAndStatusIn(
            Integer journeyId,
            Integer seatId,
            List<BookingStatus> statuses
    );
}
