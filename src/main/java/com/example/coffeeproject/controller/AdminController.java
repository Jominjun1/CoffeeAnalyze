package com.example.coffeeproject.controller;

import com.example.coffeeproject.service.AdminService;
import com.example.coffeeproject.service.CoffeeService;
import com.example.coffeeproject.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/admin")
@RestController
@Controller
public class AdminController {
    private final AdminService adminService;
    private final CoffeeService coffeeService;
    private final UserService userService;

    public AdminController(AdminService adminService, CoffeeService coffeeService, UserService userService) {
        this.adminService = adminService;
        this.coffeeService = coffeeService;
        this.userService = userService;
    }
}
