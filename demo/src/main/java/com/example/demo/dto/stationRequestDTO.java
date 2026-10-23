package com.example.demo.dto;
import jakarta.validation.constraints.NotBlank;
public class stationRequestDTO {
    @NotBlank(message="Station cannot be empty")
    private String name;
    @NotBlank (message="Station code cannot be empty")
    private String code;
    public String getName(){
        return name;
    }
    public String getCode(){
        return code;
    }
    public void setName(String name)
    {
        this.name=name;
    }
    public void setCode(String code){
        this.code=code;
    }

}
