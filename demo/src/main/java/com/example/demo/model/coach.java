package com.example.demo.model;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
@Entity
@Table(name="coaches")
public class coach {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @JoinColumn(name="train_id",nullable=false)
    private Train train;
    @Column(name="couch_number",nullable=false)
    private String coachNumber;
    @Column(nullable=false)
    private String coachType;
    public Integer getId(){
        return id;
    }
    public Train getTrain(){
        return train;
    }
    public String getCoachNumber(){
        return coachNumber;
    }
    public String getCoachType(){
        return coachType;
    }
    public void setTrain(Train train){
        this.train=train;
    }
    @JsonAlias("couchNumber")
    public void setCoachNumber(String coachNumber){
        this.coachNumber=coachNumber;
    }
    @JsonAlias("couchType")
    public void setCoachType(String coachType){
        this.coachType=coachType;
    }
}
