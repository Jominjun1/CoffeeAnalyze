package com.example.coffeeproject.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;


@Getter
@Setter
@Entity
@Table(name="A_Twosome_Place")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "Twosome_id")
public class ATwosomePlace {

    @Id
    @Column(name="Twosome_id")
    private int Twosome_id;
}
