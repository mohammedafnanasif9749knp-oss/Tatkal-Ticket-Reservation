package com.example.demo.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "journeys")
public class journey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;

    @Column(nullable = false)
    private LocalDate journeyDate;

    public Integer getId() {
        return id;
    }

    public Train getTrain() {
        return train;
    }

    public LocalDate getJourneyDate() {
        return journeyDate;
    }

    public void setTrain(Train train) {
        this.train = train;
    }

    public void setJourneyDate(LocalDate journeyDate) {
        this.journeyDate = journeyDate;
    }
}
