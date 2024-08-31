package com.example.coffeeproject.service;

import com.example.coffeeproject.respoitory.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CoffeeService {

    private final ATwosomePlaceRepository atwosomePlaceRepository;
    private final ComposeRepository composeRepository;
    private final EDIYARepository ediyaRepository;
    private final Ingredient ingredient;
    private final MegaRepository megaRepository;
    private final PaiksRepository paiksRepository;
    private final StarbucksRepository starbucksRepository;

    @Autowired
    public CoffeeService(ATwosomePlaceRepository atwosomePlaceRepository, ComposeRepository composeRepository, EDIYARepository ediyaRepository,
                         Ingredient ingredient, MegaRepository megaRepository, PaiksRepository paiksRepository, StarbucksRepository starbucksRepository) {
        this.atwosomePlaceRepository = atwosomePlaceRepository;
        this.composeRepository = composeRepository;
        this.ediyaRepository = ediyaRepository;
        this.ingredient = ingredient;
        this.megaRepository = megaRepository;
        this.paiksRepository = paiksRepository;
        this.starbucksRepository = starbucksRepository;
    }
}
