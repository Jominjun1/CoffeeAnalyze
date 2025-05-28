package com.example.coffeeproject.respoitory;

import com.example.coffeeproject.model.MegaCoffee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComposeRepository extends JpaRepository<MegaCoffee.ComposeCoffee, Integer> {
}
