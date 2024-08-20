package com.example.coffeeproject.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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

    private double Kcal; // 칼로리
    private double Saturated_fat; // 포화지방
    private double sodium; // 나트륨
    private double Protein; // 단백질
    private double Caffeine; // 카페인
    private double Sugar; // 당류
    private String Allergic_ingredients; // 알레르기 성분

}
