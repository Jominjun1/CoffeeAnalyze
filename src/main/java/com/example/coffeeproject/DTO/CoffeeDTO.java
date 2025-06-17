package com.example.coffeeproject.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CoffeeDTO {

    private String brand;
    private String name;
    private String engName;
    private String note;
    private String imageUrl;
    private IngredientDTO ingredientDTO;

}
