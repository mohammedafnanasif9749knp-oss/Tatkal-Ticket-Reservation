package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.journey;

public interface journeyrepository extends JpaRepository<journey, Integer> {
}
