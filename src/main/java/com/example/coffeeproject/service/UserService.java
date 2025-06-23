package com.example.coffeeproject.service;

import com.example.coffeeproject.DTO.CoffeeDTO;
import com.example.coffeeproject.DTO.IngredientDTO;
import com.example.coffeeproject.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.coffeeproject.repository.*;

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
        
        // 검색어 변형 생성
        List<String> searchVariations = generateSearchVariations(name);
        
        // 각 브랜드별로 모든 변형으로 검색
        for (String variation : searchVariations) {
            // 메가커피 검색
            for (MegaCoffee m : megaRepository.findByNameContainingIgnoreCase(variation)) {
                CoffeeDTO dto = mapToDTO(m);
                if (!isDuplicate(coffees, dto)) {
                    coffees.add(dto);
                }
            }
            
            // 빽다방 검색
            for (PaiksCoffee p : paiksRepository.findByNameContainingIgnoreCase(variation)) {
                CoffeeDTO dto = mapToDTO(p);
                if (!isDuplicate(coffees, dto)) {
                    coffees.add(dto);
                }
            }
            
            // 이디야 검색
            for (EDIYACoffee e : ediyaRepository.findByNameContainingIgnoreCase(variation)) {
                CoffeeDTO dto = mapToDTO(e);
                if (!isDuplicate(coffees, dto)) {
                    coffees.add(dto);
                }
            }
            
            // 컴포즈 검색
            for (ComposeCoffee c : composeRepository.findByNameContainingIgnoreCase(variation)) {
                CoffeeDTO dto = mapToDTO(c);
                if (!isDuplicate(coffees, dto)) {
                    coffees.add(dto);
                }
            }
            
            // 스타벅스 검색
            for (Starbucks s : starbucksRepository.findByNameContainingIgnoreCase(variation)) {
                CoffeeDTO dto = mapToDTO(s);
                if (!isDuplicate(coffees, dto)) {
                    coffees.add(dto);
                }
            }
            
            // 투썸 검색
            for (ATwosomePlace a : atwosomePlaceRepository.findByNameContainingIgnoreCase(variation)) {
                CoffeeDTO dto = mapToDTO(a);
                if (!isDuplicate(coffees, dto)) {
                    coffees.add(dto);
                }
            }
        }
        
        return coffees;
    }
    
    // 검색어 변형 생성 메서드
    private List<String> generateSearchVariations(String name) {
        List<String> variations = new ArrayList<>();
        
        // 원본 검색어 추가
        variations.add(name.trim());
        
        // 공백 제거 버전 추가
        String noSpace = name.replaceAll("\\s+", "");
        if (!noSpace.equals(name.trim())) {
            variations.add(noSpace);
        }
        
        // 공백 추가 버전들 생성
        String[] words = name.trim().split("\\s+");
        if (words.length > 1) {
            // 이미 공백이 있는 경우, 공백을 제거한 버전도 추가
            variations.add(String.join("", words));
        } else if (words.length == 1 && words[0].length() > 2) {
            // 단일 단어인 경우, 가능한 공백 조합 생성
            String word = words[0];
            for (int i = 1; i < word.length(); i++) {
                variations.add(word.substring(0, i) + " " + word.substring(i));
            }
        }
        
        return variations;
    }
    
    // 중복 체크 메서드
    private boolean isDuplicate(List<CoffeeDTO> coffees, CoffeeDTO newCoffee) {
        return coffees.stream()
                .anyMatch(existing -> existing.getBrand().equals(newCoffee.getBrand()) 
                        && existing.getName().equals(newCoffee.getName()));
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

}
