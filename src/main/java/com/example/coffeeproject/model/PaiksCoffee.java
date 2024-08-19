package com.example.coffeeproject.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name="Paiks_Coffee")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "Paiks_id")
public class PaiksCoffee {

    @Id
    @Column(name="Paiks_id")
    private int Paiks_id;
}
