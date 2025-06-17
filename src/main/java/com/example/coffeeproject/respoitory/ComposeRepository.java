package com.example.coffeeproject.respoitory;

import com.example.coffeeproject.model.ComposeCoffee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComposeRepository extends JpaRepository<ComposeCoffee, Integer> {
    List<ComposeCoffee> findByNameContainingIgnoreCase(String name);
}
