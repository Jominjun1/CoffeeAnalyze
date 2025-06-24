package com.example.coffeeproject.Coffee.Repository;

import com.example.coffeeproject.Coffee.Model.PaiksCoffee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaiksRepository extends JpaRepository<PaiksCoffee, Integer> {
    List<PaiksCoffee> findByNameContainingIgnoreCase(String name);
    Optional<PaiksCoffee> findByName(String name);
} 