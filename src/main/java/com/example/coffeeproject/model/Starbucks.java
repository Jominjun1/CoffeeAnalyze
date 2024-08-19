package com.example.coffeeproject.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name="StarBucks")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "StarBucks_id")
public class Starbucks {

    @Id
    @Column(name="StarBucks_id")
    private int StarBucks_id;
}
