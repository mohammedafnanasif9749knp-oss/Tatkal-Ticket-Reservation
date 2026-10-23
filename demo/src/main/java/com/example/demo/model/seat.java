package com.example.demo.model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
@Entity
@Table(name="seats")
public class seat {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @JoinColumn(name="coach_id",nullable=false)
    private coach coach;
    @Column(nullable=false)
    private String seatNumber;
    @Column(nullable=false)
    private String seatType;
    public Integer getId(){
        return id;
    }
    public coach getCoach(){
        return coach;
    }
    public String getSeatNumber(){
        return seatNumber;
    }
    public String getSeatType(){
        return seatType;
    }
    public void setCoach(coach coach){
        this.coach=coach;
    }
    public void setSeatNumber(String seatNumber)
    {
        this.seatNumber=seatNumber;
    }
    public void setSeatType(String seatType){
        this.seatType=seatType;
    }
}
