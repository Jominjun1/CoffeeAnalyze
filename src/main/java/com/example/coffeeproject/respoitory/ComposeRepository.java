package com.example.coffeeproject.respoitory;

import com.example.coffeeproject.model.ATwosomePlace;
import com.example.coffeeproject.model.ComposeCoffee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComposeRepository extends JpaRepository<ComposeCoffee, Integer> {
}
