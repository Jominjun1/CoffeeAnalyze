package com.example.coffeeproject.respoitory;

import com.example.coffeeproject.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
