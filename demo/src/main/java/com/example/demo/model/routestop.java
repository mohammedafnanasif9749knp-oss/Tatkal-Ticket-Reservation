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
@Table(name="route_stops")
public class routestop {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @JoinColumn(name="route_id",nullable=false)
    private route route;
    @ManyToOne
    @JoinColumn(name="station_id",nullable=false)
    private station station;
    @Column(nullable=false)
    private Integer stopOrder;
    public Integer getId(){
        return id;
    }
    public route getRoute(){
        return route;
    }
    public station getStation(){
        return station;
    }
    public Integer getStopOrder(){
        return stopOrder;
    }
    public void setRoute(route Route)
    {
        this.route=Route;
    }
    public void setStation(station station){
        this.station=station;
    }
    public void setStopOrder(Integer stopOrder){
        this.stopOrder=stopOrder;
    }
}
