package com.example.coffeeproject.respoitory;

import com.example.coffeeproject.model.Starbucks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StarbucksRepository extends JpaRepository<Starbucks, Integer> {
    List<Starbucks> findByNameContainingIgnoreCase(String name);
}
