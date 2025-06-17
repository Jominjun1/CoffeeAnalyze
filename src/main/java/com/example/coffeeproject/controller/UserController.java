package com.example.coffeeproject.controller;

import com.example.coffeeproject.DTO.CoffeeDTO;
import com.example.coffeeproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/user")
@RestController
@Controller
public class UserController {
    @Autowired
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<CoffeeDTO>> searchCoffee(@RequestParam String name){
        List<CoffeeDTO> coffees = userService.searchCoffeeByName(name);
        return ResponseEntity.ok(coffees);
    }
}
