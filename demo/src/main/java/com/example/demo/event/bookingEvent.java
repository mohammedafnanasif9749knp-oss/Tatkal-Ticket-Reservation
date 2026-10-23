package com.example.demo.event;

public class bookingEvent {
    private String eventType;
    private Integer bookingId;
    private Integer journeyId;
    private Integer seatId;
    public bookingEvent() {
    }

    public bookingEvent(String eventType,Integer bookingId,Integer journeyId,Integer seatId)
    {
        this.eventType=eventType;
        this.bookingId=bookingId;
        this.journeyId=journeyId;
        this.seatId=seatId;
    }
     public String getEventType() {
        return eventType;
    }

    public Integer getBookingId() {
        return bookingId;
    }

    public Integer getJourneyId() {
        return journeyId;
    }

    public Integer getSeatId() {
        return seatId;
    }
    public void setEventType(String eventType) {
    this.eventType = eventType;
}

public void setBookingId(Integer bookingId) {
    this.bookingId = bookingId;
}

public void setJourneyId(Integer journeyId) {
    this.journeyId = journeyId;
}

public void setSeatId(Integer seatId) {
    this.seatId = seatId;
}
}
