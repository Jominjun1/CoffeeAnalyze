package com.example.coffeeproject.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name="Paiks_Coffee")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "Paiks_ID")
public class PaiksCoffee {

    @Id
    @Column(name="Paiks_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int paiks_id;

    private int ounce; // 중량 1oz=29.5ml
    private int price; // 가격
    private String name; // 이름
    private String eng_name; // 영어 이름
    private String note; // 비고
    private String imageUrl; // 이미지 url

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "in_code")
    @JsonBackReference
    private Ingredient ingredients;

}
