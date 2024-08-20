package com.example.coffeeproject.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name="User")
public class User {

    @Id
    @Column(name="User_id")
    private String User_id;

    private String Name; // 이름
    private String Phone; // 핸드폰
    private String Address; // 주소
    private String Age; // 나이
    private String Email; // 이메일
}
