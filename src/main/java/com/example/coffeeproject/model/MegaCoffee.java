package com.example.coffeeproject.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name="MEGA_COFFEE")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mega_id")
public class MegaCoffee {

    @Id
    @Column(name="mega_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int mega_id;

    private double ounce; // 중량 1oz=29.5ml
    private int price; // 가격
    private String name; // 이름
    private String eng_name; // 영어 이름
    private String note; // 비고
    private String imageUrl; // 이미지 url

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "in_code")
    @JsonBackReference
    private Ingredient ingredients;

    @Getter
    @Setter
    @Entity
    @Table(name="ComposeCoffee")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "Compose_ID")
    public static class ComposeCoffee {

        @Id
        @Column(name="Compose_ID")
        @GeneratedValue(strategy = GenerationType.SEQUENCE)
        private int Compose_ID;

        private int Ounce; // 중량 1oz=29.5ml
        private int Price; // 가격
        private String Name; // 이름
        private String Eng_name; // 영어 이름
        private String Note; // 비고

        @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
        @JoinColumn(name = "in_code")
        @JsonBackReference
        private Ingredient ingredients;

    }
}
