package com.example.coffeeproject.respoitory;

import com.example.coffeeproject.model.EDIYACoffee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EDIYARepository extends JpaRepository<EDIYACoffee, Integer> {

    List<EDIYACoffee> findByNameContainingIgnoreCase(String name);
}
