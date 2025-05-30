package com.example.coffeeproject.controller;

import com.example.coffeeproject.model.Admin;
import com.example.coffeeproject.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/admin")
@RestController
@Controller
public class AdminController {
    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }
    
    // 관리자 회원 가입 API
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody Admin admin){
        return adminService.signup(admin);
    }
}
