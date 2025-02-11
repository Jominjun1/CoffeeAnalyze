package com.example.coffeeproject.controller;

import com.example.coffeeproject.service.CoffeeService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("coffee")
@Controller
@RestController
public class CoffeeMenuController {
    private final CoffeeService coffeeService;

    public CoffeeMenuController(CoffeeService coffeeService) {
        this.coffeeService = coffeeService;
    }
}
