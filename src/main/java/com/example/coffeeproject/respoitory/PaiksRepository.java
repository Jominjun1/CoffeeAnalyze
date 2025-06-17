package com.example.coffeeproject.respoitory;

import com.example.coffeeproject.model.PaiksCoffee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaiksRepository extends JpaRepository<PaiksCoffee, Integer> {

    Optional<PaiksCoffee> findByName(String name);

    List<PaiksCoffee> findByNameContainingIgnoreCase(String name);

}
