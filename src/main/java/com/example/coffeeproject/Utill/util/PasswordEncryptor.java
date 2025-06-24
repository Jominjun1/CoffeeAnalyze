package com.example.coffeeproject.Utill.util;

import com.example.coffeeproject.User.model.Admin;
import com.example.coffeeproject.User.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PasswordEncryptor {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void encryptAllAdminPasswords() {
        List<Admin> admins = adminRepository.findAll();
        for (Admin admin : admins) {
            if (!admin.getAdmin_pw().startsWith("$2a$")) { // 이미 암호화되지 않은 경우만
                String encryptedPassword = passwordEncoder.encode(admin.getAdmin_pw());
                admin.setAdmin_pw(encryptedPassword);
                adminRepository.save(admin);
            }
        }
    }

    public void encryptAdminPassword(String adminId) {
        Admin admin = adminRepository.findById(adminId).orElse(null);
        if (admin != null && !admin.getAdmin_pw().startsWith("$2a$")) {
            String encryptedPassword = passwordEncoder.encode(admin.getAdmin_pw());
            admin.setAdmin_pw(encryptedPassword);
            adminRepository.save(admin);
        }
    }
} 