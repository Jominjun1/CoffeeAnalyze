package com.example.coffeeproject.repository;

import com.example.coffeeproject.model.ATwosomePlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ATwosomePlaceRepository extends JpaRepository<ATwosomePlace, Integer> {
    List<ATwosomePlace> findByNameContainingIgnoreCase(String name);
} 