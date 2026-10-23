package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import com.example.demo.model.user;
import com.example.demo.service.userService;
@RestController
@RequestMapping("/api/users")
public class userController {
    private final userService userService;
    public userController(userService userService)
    {
this.userService=userService;
    }
    @GetMapping
    public ResponseEntity<List<user>> getAllUsers()
    {
return ResponseEntity.ok(userService.getAllUsers());
    }
    @PostMapping
    public ResponseEntity<user> createuser(@Valid @RequestBody user newuser)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(newuser));
    }
}
