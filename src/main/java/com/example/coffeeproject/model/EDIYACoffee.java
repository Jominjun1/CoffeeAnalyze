package com.example.coffeeproject.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;


@Getter
@Setter
@Entity
@Table(name="EDIYA_Coffee")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "Ediya_id")
public class EDIYACoffee {

    @Id
    @Column(name="Ediya_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int Ediya_id;

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
