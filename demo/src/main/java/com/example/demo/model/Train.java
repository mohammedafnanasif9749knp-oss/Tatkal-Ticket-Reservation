package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "trains")
public class Train {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "number", nullable = false, unique = true)
    private String trainNumber;

    @Column(nullable = false)
    private String name;

    public Integer getId() {
        return id;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public String getName() {
        return name;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public void setName(String name) {
        this.name = name;
    }
}
