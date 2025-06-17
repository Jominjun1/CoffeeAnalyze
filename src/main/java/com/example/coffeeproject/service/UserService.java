package com.example.coffeeproject.service;

import com.example.coffeeproject.DTO.CoffeeDTO;
import com.example.coffeeproject.DTO.IngredientDTO;
import com.example.coffeeproject.model.*;
import com.example.coffeeproject.respoitory.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final MegaRepository megaRepository;
    private final PaiksRepository paiksRepository;
    private final ATwosomePlaceRepository atwosomePlaceRepository;
    private final ComposeRepository composeRepository;
    private final StarbucksRepository starbucksRepository;
    private final EDIYARepository ediyaRepository;
    
    @Autowired
    public UserService(UserRepository userRepository, MegaRepository megaRepository, PaiksRepository paiksRepository, ATwosomePlaceRepository atwosomePlaceRepository, ComposeRepository composeRepository, StarbucksRepository starbucksRepository, EDIYARepository ediyaRepository) {
        this.userRepository = userRepository;
        this.megaRepository = megaRepository;
        this.paiksRepository = paiksRepository;
        this.atwosomePlaceRepository = atwosomePlaceRepository;
        this.composeRepository = composeRepository;
        this.starbucksRepository = starbucksRepository;
        this.ediyaRepository = ediyaRepository;
    }

    public ResponseEntity<String> signup(Object user){
        if(isUserIdAvailable(user)){
            saveUser(user);
            return ResponseEntity.ok("회원가입 성공");
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이미 존재하는 ID");
        }
    }

    public void saveUser(Object user) {
        userRepository.save((User)user);
    }

    public List<CoffeeDTO> searchCoffeeByName(String name){
        List<CoffeeDTO> coffees = new ArrayList<>();
        for (MegaCoffee m : megaRepository.findByNameContainingIgnoreCase(name)) {
            coffees.add(mapToDTO(m));
        }
        for (PaiksCoffee p : paiksRepository.findByNameContainingIgnoreCase(name)) {
            coffees.add(mapToDTO(p));
        }
        for (EDIYACoffee e : ediyaRepository.findByNameContainingIgnoreCase(name)) {
            coffees.add(mapToDTO(e));
        }
        for (ComposeCoffee c : composeRepository.findByNameContainingIgnoreCase(name)) {
            coffees.add(mapToDTO(c));
        }
        for (Starbucks s : starbucksRepository.findByNameContainingIgnoreCase(name)) {
            coffees.add(mapToDTO(s));
        }
        for (ATwosomePlace a : atwosomePlaceRepository.findByNameContainingIgnoreCase(name)) {
            coffees.add(mapToDTO(a));
        }

        return coffees;
    }

    private CoffeeDTO mapToDTO(MegaCoffee m) {
        return new CoffeeDTO("메가커피", m.getName(), m.getEng_name(), m.getNote(), m.getImageUrl(), toIngredientDTO(m.getIngredients()));
    }
    private CoffeeDTO mapToDTO(PaiksCoffee p) {
        return new CoffeeDTO("빽다방", p.getName(), p.getEng_name(), p.getNote(), p.getImageUrl(), toIngredientDTO(p.getIngredients()));
    }
    private CoffeeDTO mapToDTO(EDIYACoffee e) {
        return new CoffeeDTO("이디야", e.getName(), e.getEng_name(), e.getNote(), e.getImageUrl(), toIngredientDTO(e.getIngredients()));
    }
    private CoffeeDTO mapToDTO(Starbucks s) {
        return new CoffeeDTO("스타벅스", s.getName(), s.getEng_name(), s.getNote(), s.getImageUrl(), toIngredientDTO(s.getIngredients()));
    }
    private CoffeeDTO mapToDTO(ComposeCoffee c) {
        return new CoffeeDTO("컴포즈", c.getName(), c.getEng_name(), c.getNote(), c.getImageUrl(), toIngredientDTO(c.getIngredients()));
    }
    private CoffeeDTO mapToDTO(ATwosomePlace a) {
        return new CoffeeDTO("투썸", a.getName(), a.getEng_name(), a.getNote(), a.getImageUrl(), toIngredientDTO(a.getIngredients()));
    }
    private IngredientDTO toIngredientDTO(Ingredient ing) {
        return new IngredientDTO(
                ing.getKcal(),
                ing.getSaturated_fat(),
                ing.getSodium(),
                ing.getProtein(),
                ing.getCaffeine(),
                ing.getSugar(),
                ing.getAllergic_ingredients()
        );
    }
    private boolean isUserIdAvailable(Object user) {
        if (user instanceof User) {
            return !userRepository.existsById(((User) user).getUser_id());
        } else {
            throw new IllegalArgumentException("잘못된 사용자 유형");
        }
    }

}
