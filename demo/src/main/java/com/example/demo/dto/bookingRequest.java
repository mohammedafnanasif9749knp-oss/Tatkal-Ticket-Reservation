package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class bookingRequest {
    @NotNull
    @Positive
    private Integer userId;
    @NotNull
    @Positive
    private Integer journeyId;
    @NotNull
    @Positive
    private Integer seatId;
    public Integer getUserId(){
        return userId;
    }
    public Integer getJourneyId(){
        return journeyId;
    }
    public Integer getSeatId(){
        return seatId;
    }
    public void setUserId(Integer userId)
    {
        this.userId=userId;
    }
    public void setJourneyId(Integer journeyId)
    {
        this.journeyId=journeyId;
    }
    public void setSeatId(Integer seatId)
    {
        this.seatId=seatId;
    }
}
