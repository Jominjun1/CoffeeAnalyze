package com.example.coffeeproject.repository;

import com.example.coffeeproject.model.EDIYACoffee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EDIYARepository extends JpaRepository<EDIYACoffee, Integer> {
    List<EDIYACoffee> findByNameContainingIgnoreCase(String name);
    Optional<EDIYACoffee> findByName(String name);
} 