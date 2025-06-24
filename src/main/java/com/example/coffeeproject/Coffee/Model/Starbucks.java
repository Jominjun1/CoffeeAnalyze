package com.example.coffeeproject.Coffee.Model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name="StarBucks")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "starbucks_id")
public class Starbucks {

    @Id
    @Column(name="starbucks_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int starbucks_id;

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

}
