package com.example.coffeeproject.controller;


import com.example.coffeeproject.service.CoffeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/crawCoffee")
@RequiredArgsConstructor
public class CrawController {

    private final CoffeeService coffeeService;

    @GetMapping("/paiks")
    public ResponseEntity<String> crawPaiks(){
        try{
            coffeeService.crawlPaiksCoffee();
            return ResponseEntity.ok("성공");
        } catch(Exception e){
            return ResponseEntity.internalServerError().body("오류 :" + e.getMessage());
        }
    }
}
