package com.example.demo.model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonProperty;
@Entity
@Table(name="users")
public class user {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable=false)
    @NotBlank
    private String name;
    @Column(nullable=false,unique=true)
    @NotBlank
    @Email
    private String email;
    @Column(nullable=false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank
    @Size(min = 12)
    private String password;
    @Column(nullable=false)
    private String role;
    public Integer getId()
    {
        return id;
    }
    public String getName(){
        return name;
    }
    public String getEmail(){
        return email;
    }
    public String getPassword(){
        return password;
    }
    public String getRole(){
        return role;
    }
    public void setName(String name){
        this.name=name;
    }
    public void setEmail(String email)
    {
        this.email=email;
    }
    public void setPassword(String password)
    {
        this.password=password;
    }
    public void setRole(String role)
    {
        this.role=role;
    }
}
