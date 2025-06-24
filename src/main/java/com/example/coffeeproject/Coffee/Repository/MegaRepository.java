package com.example.coffeeproject.Coffee.Repository;

import com.example.coffeeproject.Coffee.Model.MegaCoffee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MegaRepository extends JpaRepository<MegaCoffee, Integer> {
    List<MegaCoffee> findByNameContainingIgnoreCase(String name);
    Optional<MegaCoffee> findByName(String name);
} 