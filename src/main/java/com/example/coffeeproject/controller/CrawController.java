package com.example.coffeeproject.controller;

import com.example.coffeeproject.service.CoffeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/crawCoffee")
public class CrawController {

    @Autowired
    private final CoffeeService coffeeService;

    public CrawController(CoffeeService coffeeService) {
        this.coffeeService = coffeeService;
    }

    @GetMapping("/paiks")
    public ResponseEntity<String> crawPaiks(){
        try{
            coffeeService.crawlPaiksCoffee();
            return ResponseEntity.ok("성공");
        } catch(Exception e){
            return ResponseEntity.internalServerError().body("오류 :" + e.getMessage());
        }
    }
    @GetMapping("/mega_coffee")
    public ResponseEntity<String> crawMegaCoffee(){
        try{
            coffeeService.crawlMegaCoffee();
            return ResponseEntity.ok("성공");
        } catch (Exception e){
            return ResponseEntity.internalServerError().body("오류 :" + e.getMessage());
        }
    }
    @GetMapping("/starBucks")
    public ResponseEntity<String> crawStarBucks(){
        try{
            coffeeService.crawlStarBucks();
            return ResponseEntity.ok("성공");
        } catch (Exception e){
            return ResponseEntity.internalServerError().body("오류 :" + e.getMessage());
        }
    }

}
