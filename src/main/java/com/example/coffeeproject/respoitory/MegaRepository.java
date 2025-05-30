package com.example.coffeeproject.respoitory;

import com.example.coffeeproject.model.MegaCoffee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MegaRepository extends JpaRepository<MegaCoffee, Integer> {
    Optional<MegaCoffee> findByName(String name);
}
