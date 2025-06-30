package com.example.coffeeproject.Coffee.Model;

import com.fasterxml.jackson.annotation.*;
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
    private double price; // 가격
    private String name; // 이름
    private String eng_name; // 영어 이름
    private String note; // 비고
    private String imageUrl; // 이미지 url

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "in_code")
    @JsonBackReference
    private Ingredient ingredients;
}
