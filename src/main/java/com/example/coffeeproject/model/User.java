package com.example.coffeeproject.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name="User")
public class User {

    @Id
    @Column(name="User_id")
    @Pattern(regexp = "[a-zA-Z0-9]{4,9}") // 영어 + 숫자조합 4자리 ~ 9자리
    private String User_id; // 아이디

    @Pattern(regexp ="(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,16}") // 특수문자 포함 8자리~16자리
    private String User_pw; // 비밀번호

    private String Name; // 이름
    private String Phone; // 핸드폰
    private String Address; // 주소
    private String Age; // 나이
    private String Email; // 이메일
}
