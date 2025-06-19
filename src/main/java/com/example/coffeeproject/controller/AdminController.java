package com.example.coffeeproject.controller;

import com.example.coffeeproject.model.Admin;
import com.example.coffeeproject.repository.AdminRepository;
import com.example.coffeeproject.service.AdminService;
import com.example.coffeeproject.util.JwtUtil;
import com.example.coffeeproject.util.PasswordEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

@RequestMapping("/admin")
@RestController
@CrossOrigin(origins = "*")
public class AdminController {
    private final AdminService adminService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private PasswordEncryptor passwordEncryptor;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }
    
    // 관리자 로그인 API
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Admin admin) {
        return adminService.login(admin);
    }
    
    // 관리자 로그아웃 API
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("로그아웃되었습니다.");
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
        return ResponseEntity.ok("모든 관리자 비밀번호가 암호화되었습니다.");
    }
    
    // 개발용: 특정 관리자 비밀번호 암호화 API
    @PostMapping("/encrypt-password/{adminId}")
    public ResponseEntity<String> encryptPassword(@PathVariable String adminId) {
        passwordEncryptor.encryptAdminPassword(adminId);
        return ResponseEntity.ok("관리자 '" + adminId + "' 비밀번호가 암호화되었습니다.");
    }
}
