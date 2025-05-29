package com.example.coffeeproject.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
@Table(name="Ingredient")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "in_code")
public class Ingredient {

    @Id
    @Column(name="in_code")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long in_code;

    private double kcal; // 칼로리
    private double saturated_fat; // 포화지방
    private double sodium; // 나트륨
    private double protein; // 단백질
    private double caffeine; // 카페인
    private double sugar; // 당류
    private String allergic_ingredients; // 알레르기 성분

    @OneToOne(mappedBy = "ingredients")
    @JsonBackReference
    private PaiksCoffee paiksCoffee;

}
