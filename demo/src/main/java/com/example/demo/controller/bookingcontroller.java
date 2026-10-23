package com.example.demo.controller;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import com.example.demo.dto.bookingRequest;
import com.example.demo.model.Booking;
import com.example.demo.service.BookingService;
@RestController
@RequestMapping("/api/bookings")
public class bookingcontroller {
    private final BookingService bookingservice;
    public bookingcontroller(BookingService bookingservice)
    {
        this.bookingservice=bookingservice;
    }
    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings(){
        return ResponseEntity.ok(bookingservice.getAllBookings());
    }
   @PostMapping
public ResponseEntity<Booking> createBooking(@Valid @RequestBody bookingRequest request) {
    Booking saved = bookingservice.createBooking(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
}
   @PostMapping("/available-seat")
   public ResponseEntity<bookingRequest> suggestAvailableSeat(@Valid @RequestBody bookingRequest request) {
       return ResponseEntity.ok(bookingservice.suggestAvailableSeat(request));
   }
   @GetMapping("/availability")
public ResponseEntity<Boolean> checkAvialability(@RequestParam Integer journeyId,@RequestParam Integer seatId)
{
    return ResponseEntity.ok(bookingservice.isSeatAvialable(journeyId,seatId));
}
@PutMapping("/{id}/cancel")
public ResponseEntity<Booking> cancelBooking(@PathVariable Integer id)
{
    return ResponseEntity.ok(bookingservice.cancelBooking(id));
}
}
