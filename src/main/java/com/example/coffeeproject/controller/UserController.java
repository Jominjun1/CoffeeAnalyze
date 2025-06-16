package com.example.coffeeproject.controller;

import com.example.coffeeproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/user")
@RestController
@Controller
public class UserController {
    @Autowired
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

//    @GetMapping("/search")
//    public ResponseEntity<String> searchCoffee(@RequestParam String name){
//
//    }
}
