package com.example.coffeeproject.model;

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
    private int Ediya_id;
}
