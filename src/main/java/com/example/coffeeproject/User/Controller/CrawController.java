package com.example.coffeeproject.User.Controller;

import com.example.coffeeproject.Coffee.service.CoffeeService;
import com.example.coffeeproject.Utill.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/craw")
@CrossOrigin(origins = "*")
public class CrawController {

    @Autowired
    private final CoffeeService coffeeService;
    private JwtUtil jwtUtil;

    public CrawController(CoffeeService coffeeService) {
        this.coffeeService = coffeeService;
    }

    @GetMapping("/paiks")
    public ResponseEntity<String> crawPaiks(HttpServletRequest request){
        if (!validateToken(request)) {
            return ResponseEntity.status(401).body("인증 필요");
        }
        try{
            coffeeService.crawlPaiksCoffee();
            return ResponseEntity.ok("성공");
        } catch(Exception e){
            return ResponseEntity.internalServerError().body("오류 :" + e.getMessage());
        }
    }
    
    @GetMapping("/mega_coffee")
    public ResponseEntity<String> crawMegaCoffee(HttpServletRequest request){
        if (!validateToken(request)) {
            return ResponseEntity.status(401).body("인증 필요");
        }
        try{
            coffeeService.crawlMegaCoffee();
            return ResponseEntity.ok("성공");
        } catch (Exception e){
            return ResponseEntity.internalServerError().body("오류 :" + e.getMessage());
        }
    }
    
    @GetMapping("/starBucks")
    public ResponseEntity<String> crawStarBucks(HttpServletRequest request){
        if (!validateToken(request)) {
            return ResponseEntity.status(401).body("인증 필요");
        }
        try{
            coffeeService.crawlStarBucks();
            return ResponseEntity.ok("성공");
        } catch (Exception e){
            return ResponseEntity.internalServerError().body("오류 :" + e.getMessage());
        }
    }

    @GetMapping("/ediya")
    public ResponseEntity<String> crawEdiya(HttpServletRequest request){
        if (!validateToken(request)) {
            return ResponseEntity.status(401).body("인증 필요");
        }
        try{
            coffeeService.crawlEdiya();
            return ResponseEntity.ok("성공");
        }catch (Exception e){
            return ResponseEntity.internalServerError().body("오류 :" + e.getMessage());
        }
    }
    @GetMapping("/compose")
    public ResponseEntity<String> crawCompose(HttpServletRequest request){
        if (!validateToken(request)) {
            return ResponseEntity.status(401).body("인증 필요");
        }try{
            coffeeService.crawlCompose();
            return ResponseEntity.ok("성공");
        }catch(Exception e){
            return ResponseEntity.internalServerError().body("오류 :" + e.getMessage());
        }
    }
    // 전체 커피 메뉴 크롤링
    @GetMapping("/allCoffee")
    public ResponseEntity<String> crawAllCoffee(HttpServletRequest request){
        if (!validateToken(request)) {
            return ResponseEntity.status(401).body("인증 필요");
        }try{
            coffeeService.crawlEdiya();
            coffeeService.crawlCompose();
            coffeeService.crawlStarBucks();
            coffeeService.crawlMegaCoffee();
            coffeeService.crawlPaiksCoffee();
            return ResponseEntity.ok("성공");
        }catch(Exception e){
            return ResponseEntity.internalServerError().body("오류 :" + e.getMessage());
        }
    }

    // JWT 토큰 검증
    private boolean validateToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return false;
        }
        token = token.substring(7);
        return jwtUtil.validateToken(token);
    }
}
