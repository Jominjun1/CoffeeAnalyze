package com.example.coffeeproject.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IngredientDTO {

    private double kcal;
    private double saturatedFat;
    private double sodium;
    private double protein;
    private double caffeine;
    private double sugar;
    private String allergicIngredients;
}
