package com.example.coffeeproject.respoitory;

import com.example.coffeeproject.model.ATwosomePlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ATwosomePlaceRepository extends JpaRepository<ATwosomePlace, Integer> {
}
