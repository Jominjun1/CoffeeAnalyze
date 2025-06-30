package com.example.coffeeproject.User.Service;

import com.example.coffeeproject.Coffee.DTO.CoffeeDTO;
import com.example.coffeeproject.Coffee.DTO.IngredientDTO;
import com.example.coffeeproject.Coffee.Model.*;
import com.example.coffeeproject.Coffee.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final MegaRepository megaRepository;
    private final PaiksRepository paiksRepository;
    private final ATwosomePlaceRepository atwosomePlaceRepository;
    private final ComposeRepository composeRepository;
    private final StarbucksRepository starbucksRepository;
    private final EDIYARepository ediyaRepository;
    
    @Autowired
    public UserService( MegaRepository megaRepository, PaiksRepository paiksRepository, ATwosomePlaceRepository atwosomePlaceRepository,
                        ComposeRepository composeRepository, StarbucksRepository starbucksRepository, EDIYARepository ediyaRepository) {
        this.megaRepository = megaRepository;
        this.paiksRepository = paiksRepository;
        this.atwosomePlaceRepository = atwosomePlaceRepository;
        this.composeRepository = composeRepository;
        this.starbucksRepository = starbucksRepository;
        this.ediyaRepository = ediyaRepository;
    }
    public List<CoffeeDTO> searchCoffeeByName(String name){
        List<CoffeeDTO> coffees = new ArrayList<>();
        List<String> searchVariations = generateSearchVariations(name);

        for (String variation : searchVariations) {
            for (MegaCoffee m : megaRepository.findByNameContainingIgnoreCase(variation)) {
                CoffeeDTO dto = mapToDTO(m);
                if (isDuplicate(coffees, dto)) {coffees.add(dto);}
            }

            for (PaiksCoffee p : paiksRepository.findByNameContainingIgnoreCase(variation)) {
                CoffeeDTO dto = mapToDTO(p);
                if (isDuplicate(coffees, dto)) {coffees.add(dto);}
            }

            for (EDIYACoffee e : ediyaRepository.findByNameContainingIgnoreCase(variation)) {
                CoffeeDTO dto = mapToDTO(e);
                if (isDuplicate(coffees, dto)) {coffees.add(dto);}
            }

            for (ComposeCoffee c : composeRepository.findByNameContainingIgnoreCase(variation)) {
                CoffeeDTO dto = mapToDTO(c);
                if (isDuplicate(coffees, dto)) {coffees.add(dto);}
            }

            for (Starbucks s : starbucksRepository.findByNameContainingIgnoreCase(variation)) {
                CoffeeDTO dto = mapToDTO(s);
                if (isDuplicate(coffees, dto)) {coffees.add(dto);}
            }

            for (ATwosomePlace a : atwosomePlaceRepository.findByNameContainingIgnoreCase(variation)) {
                CoffeeDTO dto = mapToDTO(a);
                if (isDuplicate(coffees, dto)) {coffees.add(dto);}
            }
        }
        return coffees;
    }

    private List<String> generateSearchVariations(String name) {
        List<String> variations = new ArrayList<>();
        variations.add(name.trim());
        String noSpace = name.replaceAll("\\s+", "");

        if (!noSpace.equals(name.trim())) {
            variations.add(noSpace);
        }

        String[] words = name.trim().split("\\s+");

        if (words.length > 1) {
            variations.add(String.join("", words));
        } else if (words.length == 1 && words[0].length() > 2) {
            String word = words[0];
            for (int i = 1; i < word.length(); i++) {
                variations.add(word.substring(0, i) + " " + word.substring(i));
            }
        }
        return variations;
    }

    private boolean isDuplicate(List<CoffeeDTO> coffees, CoffeeDTO newCoffee) {
        return coffees.stream().noneMatch(existing -> existing.getBrand().equals(newCoffee.getBrand()) && existing.getName().equals(newCoffee.getName()));
    }

    private CoffeeDTO mapToDTO(MegaCoffee m) {
        String size = m.getOunce() > 0 ? String.format("%.1fml", m.getOunce()) : "";
        return new CoffeeDTO("메가커피", m.getName(), m.getEng_name(), m.getNote(), m.getImageUrl(), size, toIngredientDTO(m.getIngredients()));
    }
    private CoffeeDTO mapToDTO(PaiksCoffee p) {
        String size = p.getOunce() > 0 ? String.format("%.1fml", p.getOunce()) : "";
        return new CoffeeDTO("빽다방", p.getName(), p.getEng_name(), p.getNote(), p.getImageUrl(), size, toIngredientDTO(p.getIngredients()));
    }
    private CoffeeDTO mapToDTO(EDIYACoffee e) {
        String size = e.getOunce() > 0 ? String.format("%.1fml", e.getOunce()) : "";
        return new CoffeeDTO("이디야", e.getName(), e.getEng_name(), e.getNote(), e.getImageUrl(), size, toIngredientDTO(e.getIngredients()));
    }
    private CoffeeDTO mapToDTO(Starbucks s) {
        String size = s.getOunce() > 0 ? String.format("%.1fml", s.getOunce()) : "";
        return new CoffeeDTO("스타벅스", s.getName(), s.getEng_name(), s.getNote(), s.getImageUrl(), size, toIngredientDTO(s.getIngredients()));
    }
    private CoffeeDTO mapToDTO(ComposeCoffee c) {
        String size = c.getOunce() > 0 ? String.format("%.1fml", c.getOunce()) : "";
        return new CoffeeDTO("컴포즈", c.getName(), c.getEng_name(), c.getNote(), c.getImageUrl(), size, toIngredientDTO(c.getIngredients()));
    }
    private CoffeeDTO mapToDTO(ATwosomePlace a) {
        String size = a.getOunce() > 0 ? String.format("%.1fml", a.getOunce()) : "";
        return new CoffeeDTO("투썸", a.getName(), a.getEng_name(), a.getNote(), a.getImageUrl(), size, toIngredientDTO(a.getIngredients()));
    }
    private IngredientDTO toIngredientDTO(Ingredient ing) {
        return new IngredientDTO(ing.getKcal(), ing.getSaturated_fat(), ing.getSodium(), ing.getProtein(), ing.getCaffeine(), ing.getSugar(), ing.getAllergic_ingredients());
    }

    public void updateCoffees(List<CoffeeDTO> coffeeUpdates) {
        for (CoffeeDTO coffeeDTO : coffeeUpdates) {
            updateCoffee(coffeeDTO);
        }
    }

    private void updateCoffee(CoffeeDTO coffeeDTO) {
        String brand = coffeeDTO.getBrand();
        switch (brand) {
            case "메가커피":
                updateMegaCoffee(coffeeDTO);
                break;
            case "빽다방":
                updatePaiksCoffee(coffeeDTO);
                break;
            case "이디야":
                updateEdiyaCoffee(coffeeDTO);
                break;
            case "스타벅스":
                updateStarbucksCoffee(coffeeDTO);
                break;
            case "컴포즈":
                updateComposeCoffee(coffeeDTO);
                break;
            case "투썸":
                updateATwosomeCoffee(coffeeDTO);
                break;
            default:
                throw new IllegalArgumentException("지원하지 않는 브랜드: " + brand);
        }
    }

    private void updateMegaCoffee(CoffeeDTO coffeeDTO) {
        MegaCoffee coffee = megaRepository.findByName(coffeeDTO.getName()).orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없음: " + coffeeDTO.getName()));

        coffee.setName(coffeeDTO.getName());
        coffee.setEng_name(coffeeDTO.getEngName());
        coffee.setNote(coffeeDTO.getNote());
        coffee.setImageUrl(coffeeDTO.getImageUrl());

        Ingredient ingredient = coffee.getIngredients();
        IngredientDTO ingredientDTO = coffeeDTO.getIngredientDTO();
        ingredient.setKcal(ingredientDTO.getKcal());
        ingredient.setCaffeine(ingredientDTO.getCaffeine());
        ingredient.setSodium(ingredientDTO.getSodium());
        ingredient.setSugar(ingredientDTO.getSugar());
        ingredient.setSaturated_fat(ingredientDTO.getSaturatedFat());
        ingredient.setProtein(ingredientDTO.getProtein());
        ingredient.setAllergic_ingredients(ingredientDTO.getAllergicIngredients());
        
        megaRepository.save(coffee);
    }

    private void updatePaiksCoffee(CoffeeDTO coffeeDTO) {
        PaiksCoffee coffee = paiksRepository.findByName(coffeeDTO.getName()).orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없음: " + coffeeDTO.getName()));

        coffee.setName(coffeeDTO.getName());
        coffee.setEng_name(coffeeDTO.getEngName());
        coffee.setNote(coffeeDTO.getNote());
        coffee.setImageUrl(coffeeDTO.getImageUrl());

        Ingredient ingredient = coffee.getIngredients();
        IngredientDTO ingredientDTO = coffeeDTO.getIngredientDTO();
        ingredient.setKcal(ingredientDTO.getKcal());
        ingredient.setCaffeine(ingredientDTO.getCaffeine());
        ingredient.setSodium(ingredientDTO.getSodium());
        ingredient.setSugar(ingredientDTO.getSugar());
        ingredient.setSaturated_fat(ingredientDTO.getSaturatedFat());
        ingredient.setProtein(ingredientDTO.getProtein());
        ingredient.setAllergic_ingredients(ingredientDTO.getAllergicIngredients());
        
        paiksRepository.save(coffee);
    }

    private void updateEdiyaCoffee(CoffeeDTO coffeeDTO) {
        EDIYACoffee coffee = ediyaRepository.findByName(coffeeDTO.getName()).orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없음: " + coffeeDTO.getName()));

        coffee.setName(coffeeDTO.getName());
        coffee.setEng_name(coffeeDTO.getEngName());
        coffee.setNote(coffeeDTO.getNote());
        coffee.setImageUrl(coffeeDTO.getImageUrl());

        Ingredient ingredient = coffee.getIngredients();
        IngredientDTO ingredientDTO = coffeeDTO.getIngredientDTO();
        ingredient.setKcal(ingredientDTO.getKcal());
        ingredient.setCaffeine(ingredientDTO.getCaffeine());
        ingredient.setSodium(ingredientDTO.getSodium());
        ingredient.setSugar(ingredientDTO.getSugar());
        ingredient.setSaturated_fat(ingredientDTO.getSaturatedFat());
        ingredient.setProtein(ingredientDTO.getProtein());
        ingredient.setAllergic_ingredients(ingredientDTO.getAllergicIngredients());
        
        ediyaRepository.save(coffee);
    }

    private void updateStarbucksCoffee(CoffeeDTO coffeeDTO) {
        Starbucks coffee = starbucksRepository.findByName(coffeeDTO.getName()).orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없음: " + coffeeDTO.getName()));

        coffee.setName(coffeeDTO.getName());
        coffee.setEng_name(coffeeDTO.getEngName());
        coffee.setNote(coffeeDTO.getNote());
        coffee.setImageUrl(coffeeDTO.getImageUrl());

        Ingredient ingredient = coffee.getIngredients();
        IngredientDTO ingredientDTO = coffeeDTO.getIngredientDTO();
        ingredient.setKcal(ingredientDTO.getKcal());
        ingredient.setCaffeine(ingredientDTO.getCaffeine());
        ingredient.setSodium(ingredientDTO.getSodium());
        ingredient.setSugar(ingredientDTO.getSugar());
        ingredient.setSaturated_fat(ingredientDTO.getSaturatedFat());
        ingredient.setProtein(ingredientDTO.getProtein());
        ingredient.setAllergic_ingredients(ingredientDTO.getAllergicIngredients());
        
        starbucksRepository.save(coffee);
    }

    private void updateComposeCoffee(CoffeeDTO coffeeDTO) {
        ComposeCoffee coffee = composeRepository.findByName(coffeeDTO.getName()).orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없음: " + coffeeDTO.getName()));

        coffee.setName(coffeeDTO.getName());
        coffee.setEng_name(coffeeDTO.getEngName());
        coffee.setNote(coffeeDTO.getNote());
        coffee.setImageUrl(coffeeDTO.getImageUrl());

        Ingredient ingredient = coffee.getIngredients();
        IngredientDTO ingredientDTO = coffeeDTO.getIngredientDTO();
        ingredient.setKcal(ingredientDTO.getKcal());
        ingredient.setCaffeine(ingredientDTO.getCaffeine());
        ingredient.setSodium(ingredientDTO.getSodium());
        ingredient.setSugar(ingredientDTO.getSugar());
        ingredient.setSaturated_fat(ingredientDTO.getSaturatedFat());
        ingredient.setProtein(ingredientDTO.getProtein());
        ingredient.setAllergic_ingredients(ingredientDTO.getAllergicIngredients());
        
        composeRepository.save(coffee);
    }

    private void updateATwosomeCoffee(CoffeeDTO coffeeDTO) {
        ATwosomePlace coffee = atwosomePlaceRepository.findByName(coffeeDTO.getName()).orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없음: " + coffeeDTO.getName()));

        coffee.setName(coffeeDTO.getName());
        coffee.setEng_name(coffeeDTO.getEngName());
        coffee.setNote(coffeeDTO.getNote());
        coffee.setImageUrl(coffeeDTO.getImageUrl());

        Ingredient ingredient = coffee.getIngredients();
        IngredientDTO ingredientDTO = coffeeDTO.getIngredientDTO();
        ingredient.setKcal(ingredientDTO.getKcal());
        ingredient.setCaffeine(ingredientDTO.getCaffeine());
        ingredient.setSodium(ingredientDTO.getSodium());
        ingredient.setSugar(ingredientDTO.getSugar());
        ingredient.setSaturated_fat(ingredientDTO.getSaturatedFat());
        ingredient.setProtein(ingredientDTO.getProtein());
        ingredient.setAllergic_ingredients(ingredientDTO.getAllergicIngredients());
        
        atwosomePlaceRepository.save(coffee);
    }

}
