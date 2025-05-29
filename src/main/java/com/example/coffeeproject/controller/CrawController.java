package com.example.coffeeproject.controller;


import com.example.coffeeproject.model.PaiksCoffee;
import com.example.coffeeproject.respoitory.PaiksRepository;
import com.example.coffeeproject.service.CoffeeService;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/crawCoffee")
@RequiredArgsConstructor
public class CrawController {

    private final CoffeeService coffeeService;
    private final PaiksRepository paiksRepository;

    @Autowired
    public CrawController(PaiksRepository paiksRepository , CoffeeService coffeeService) {
        this.paiksRepository = paiksRepository;
        this.coffeeService = coffeeService;
    }

    @GetMapping("/paiks")
    public ResponseEntity<String> crawPaiks(){
        try{
            coffeeService.crawlPaiksCoffee();
            return ResponseEntity.ok("성공");
        } catch(Exception e){
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("오류 :" + e.getMessage());
        }
    }
    @GetMapping("/paiksAll")
    public List<PaiksCoffee> getAllPaiks(){
        return paiksRepository.findAll();
    }
}
