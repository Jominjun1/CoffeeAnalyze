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
        Optional<Admin> foundAdmin = adminRepository.findById(admin.getAdmin_id());
        if (foundAdmin.isPresent()) {
            Admin existingAdmin = foundAdmin.get();
            // 암호화된 비밀번호 비교
            if (passwordEncoder.matches(admin.getAdmin_pw(), existingAdmin.getAdmin_pw())) {
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
