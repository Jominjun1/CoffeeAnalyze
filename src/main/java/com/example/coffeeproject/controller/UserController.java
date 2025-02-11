package com.example.coffeeproject.controller;

import com.example.coffeeproject.service.CoffeeService;
import com.example.coffeeproject.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/user")
@RestController
@Controller
public class UserController {
    private final UserService userService;
    private final CoffeeService coffeeService;

    public UserController(UserService userService, CoffeeService coffeeService) {
        this.userService = userService;
        this.coffeeService = coffeeService;
    }
}
