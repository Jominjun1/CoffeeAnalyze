package com.example.coffeeproject.User.Controller;

import com.example.coffeeproject.Coffee.DTO.CoffeeDTO;
import com.example.coffeeproject.User.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/getCoffee")
@RestController
@Controller
@CrossOrigin(origins = "http://localhost:5173")
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

    @PutMapping("/update")
    public ResponseEntity<String> updateCoffee(@RequestBody List<CoffeeDTO> coffeeUpdates) {
        try {
            userService.updateCoffees(coffeeUpdates);
            return ResponseEntity.ok("메뉴가 성공적으로 업데이트되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("업데이트 실패: " + e.getMessage());
        }
    }
}
