package com.example.coffeeproject.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name="MEGE_COFFEE")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "Mega_id")
public class MegaCoffee {

    @Id
    @Column(name="Mega_id")
    private int Mega_id;
}
