package com.example.coffeeproject.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name="ComposeCoffee")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "Compose_id")
public class ComposeCoffee {

    @Id
    @Column(name="Compose_id")
    private int Compose_id;

}
