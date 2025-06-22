package com.example.coffeeproject.service;

import com.example.coffeeproject.model.Admin;
import com.example.coffeeproject.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = adminRepository.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("관리자 찾을 수 없음: " + username));

        return new User(admin.getAdmin_id(), admin.getAdmin_pw(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
} 