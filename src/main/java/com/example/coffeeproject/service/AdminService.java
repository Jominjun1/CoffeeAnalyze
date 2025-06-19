package com.example.coffeeproject.service;

import com.example.coffeeproject.model.Admin;
import com.example.coffeeproject.repository.AdminRepository;
import com.example.coffeeproject.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AdminService {
    private final AdminRepository adminRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public ResponseEntity<?> login(Admin admin) {
        if (admin.getAdmin_id() == null || admin.getAdmin_id().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("관리자 ID를 입력해주세요.");
        }
        if (admin.getAdmin_pw() == null || admin.getAdmin_pw().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("비밀번호를 입력해주세요.");
        }
        
        Optional<Admin> foundAdmin = adminRepository.findById(admin.getAdmin_id());
        if (foundAdmin.isPresent()) {
            Admin existingAdmin = foundAdmin.get();
            
            // 비밀번호 검증 (BCrypt 또는 평문 비교)
            boolean passwordMatches = false;
            String storedPassword = existingAdmin.getAdmin_pw();
            
            if (storedPassword.startsWith("$2a$")) {
                // BCrypt로 암호화된 비밀번호
                passwordMatches = passwordEncoder.matches(admin.getAdmin_pw(), storedPassword);
            } else {
                // 평문 비밀번호 (임시 처리)
                passwordMatches = admin.getAdmin_pw().equals(storedPassword);
            }
            
            if (passwordMatches) {
                String token = jwtUtil.generateToken(existingAdmin.getAdmin_id(), existingAdmin.getName());
                Map<String, Object> response = new HashMap<>();
                response.put("message", "로그인 성공");
                response.put("token", token);
                response.put("admin", existingAdmin);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호가 일치하지 않습니다.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("존재하지 않는 관리자 ID입니다.");
        }
    }
}
