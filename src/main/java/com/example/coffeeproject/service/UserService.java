package com.example.coffeeproject.service;

import com.example.coffeeproject.DTO.CoffeeDTO;
import com.example.coffeeproject.DTO.IngredientDTO;
import com.example.coffeeproject.model.Ingredient;
import com.example.coffeeproject.model.MegaCoffee;
import com.example.coffeeproject.model.PaiksCoffee;
import com.example.coffeeproject.model.User;
import com.example.coffeeproject.respoitory.MegaRepository;
import com.example.coffeeproject.respoitory.PaiksRepository;
import com.example.coffeeproject.respoitory.UserRepository;
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

    @Autowired
    public UserService(UserRepository userRepository, MegaRepository megaRepository, PaiksRepository paiksRepository) {
        this.userRepository = userRepository;
        this.megaRepository = megaRepository;
        this.paiksRepository = paiksRepository;
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
        return coffees;
    }

    private CoffeeDTO mapToDTO(MegaCoffee m) {
        return new CoffeeDTO("메가커피", m.getName(), m.getEng_name(), m.getNote(), m.getImageUrl(), toIngredientDTO(m.getIngredients()));
    }

    private CoffeeDTO mapToDTO(PaiksCoffee p) {
        return new CoffeeDTO("빽다방", p.getName(), p.getEng_name(), p.getNote(), p.getImageUrl(), toIngredientDTO(p.getIngredients()));
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
