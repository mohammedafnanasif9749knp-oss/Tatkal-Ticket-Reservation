package com.example.demo.service;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.model.user;
import com.example.demo.repository.userRepository;
@Service
public class userService {
    private final userRepository userRepository;
    private final PasswordEncoder passwordEncoder;
public userService(userRepository userRepository, PasswordEncoder passwordEncoder)
{
    this.userRepository=userRepository;
    this.passwordEncoder = passwordEncoder;
}
public List<user> getAllUsers()
{
    return userRepository.findAll();
}
public user createUser(user newuser)
{
newuser.setRole("USER");
newuser.setPassword(passwordEncoder.encode(newuser.getPassword()));
return userRepository.save(newuser);
}
}
