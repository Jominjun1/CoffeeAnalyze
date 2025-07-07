package com.example.coffeeproject.User.Controller;

import com.example.coffeeproject.User.model.Admin;
import com.example.coffeeproject.User.repository.AdminRepository;
import com.example.coffeeproject.User.Service.AdminService;
import com.example.coffeeproject.Utill.util.JwtUtil;
import com.example.coffeeproject.Utill.util.PasswordEncryptor;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

@RequestMapping("/admin")
@RestController
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private final AdminService adminService;
    private JwtUtil jwtUtil;
    private AdminRepository adminRepository;
    private PasswordEncryptor passwordEncryptor;
    private PasswordEncoder passwordEncoder;


    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }
    
    // 관리자 로그인 API
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody Admin admin) {
        if (admin == null) {
            return ResponseEntity.badRequest().body("로그인 정보 없음");
        }
        return adminService.login(admin);
    }
    
    // 관리자 로그아웃 API
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("로그아웃 완료");
    }
    
    // 현재 로그인된 관리자 정보 확인 API
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentAdmin(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (jwtUtil.validateToken(token)) {
                String adminId = jwtUtil.extractAdminId(token);
                Optional<Admin> admin = adminRepository.findById(adminId);
                if (admin.isPresent()) {
                    return ResponseEntity.ok(admin.get());
                }
            }
        }
        return ResponseEntity.status(401).body("인증 실패");
    }
    
    // 개발용: 모든 관리자 비밀번호 암호화 API
    @PostMapping("/encrypt-passwords")
    public ResponseEntity<String> encryptAllPasswords() {
        passwordEncryptor.encryptAllAdminPasswords();
        return ResponseEntity.ok("암호화 완료");
    }
    
    // 개발용: 특정 관리자 비밀번호 암호화 API
    @PostMapping("/encrypt-password/{adminId}")
    public ResponseEntity<String> encryptPassword(@PathVariable String adminId) {
        passwordEncryptor.encryptAdminPassword(adminId);
        return ResponseEntity.ok("관리자 '" + adminId + "' 비밀번호 암호화");
    }
    
    // 크롤링 시작 API
    @PostMapping("/start-crawling")
    public ResponseEntity<?> startCrawling(HttpServletRequest request) {
        // JWT 토큰 검증
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("인증 토큰 필요");
        }
        
        token = token.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).body("유효하지 않은 토큰");
        }
        
        try {
            // 크롤링 서비스 호출 (비동기로 실행)
            // 여기서는 간단히 성공 메시지만 반환
            // 실제 크롤링 로직은 별도 서비스에서 처리
            return ResponseEntity.ok("크롤링 시작");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("크롤링 중 오류가 발생:  " + e.getMessage());
        }
    }
}
