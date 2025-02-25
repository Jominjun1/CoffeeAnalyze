package com.example.coffeeproject.service;

import com.example.coffeeproject.model.Admin;
import com.example.coffeeproject.respoitory.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    private static AdminRepository adminRepository = null;

    @Autowired
    public AdminService(AdminRepository adminRepository) {
        AdminService.adminRepository = adminRepository;
    }

    public static ResponseEntity<String> signup(Object admin){
        if(isUserIdAvailable(admin)){
            saveUser(admin);
            return ResponseEntity.ok("회원가입 성공");
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이미 존재하는 ID");
        }
    }

    public static void saveUser(Object admin) {
        adminRepository.save((Admin)admin);
    }
    private static boolean isUserIdAvailable(Object admin) {
        if (admin instanceof Admin) {
            return !adminRepository.existsById(((Admin) admin).getAdmin_ID());
        } else {
            throw new IllegalArgumentException("잘못된 사용자 유형");
        }
    }
}
