package com.example.coffeeproject.service;

import com.example.coffeeproject.model.User;
import com.example.coffeeproject.respoitory.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ResponseEntity<String> signup(Object user){
        if(isUserIdAvailable(user)){
            saveUser(user);
            return ResponseEntity.ok("회원가입 성공");
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이미 존재하는 ID");
        }
    }

    public void saveUser(Object user) {
        userRepository.save((User)user);
    }
    private boolean isUserIdAvailable(Object user) {
        if (user instanceof User) {
            return !userRepository.existsById(((User) user).getUser_id());
        } else {
            throw new IllegalArgumentException("잘못된 사용자 유형");
        }
    }

}
