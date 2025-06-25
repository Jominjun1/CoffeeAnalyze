package com.example.coffeeproject.Coffee.Repository;

import com.example.coffeeproject.Coffee.Model.ATwosomePlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ATwosomePlaceRepository extends JpaRepository<ATwosomePlace, Integer> {
    List<ATwosomePlace> findByNameContainingIgnoreCase(String name);
    Optional<ATwosomePlace> findByName(String name);
} 