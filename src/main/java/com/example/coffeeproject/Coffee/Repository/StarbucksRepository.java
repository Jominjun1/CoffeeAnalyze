package com.example.coffeeproject.Coffee.Repository;

import com.example.coffeeproject.Coffee.Model.Starbucks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StarbucksRepository extends JpaRepository<Starbucks, Integer> {
    List<Starbucks> findByNameContainingIgnoreCase(String name);
    Optional<Starbucks> findByName(String name);
} 