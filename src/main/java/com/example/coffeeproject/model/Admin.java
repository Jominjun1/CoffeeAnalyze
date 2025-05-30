package com.example.coffeeproject.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="admin")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "admin_id")
public class Admin {

    @Id
    @Column(name="admin_id")
    @Pattern(regexp = "[a-zA-Z0-9]{4,9}") // 영어 + 숫자조합 4자리 ~ 9자리
    private String admin_id; // 아이디

    @Pattern(regexp ="(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,16}") // 특수문자 포함 8자리~16자리
    private String admin_pw; // 비밀번호

    private String name; // 이름
    private String phone; // 핸드폰
    private String address; // 주소
    private String age; // 나이
    private String email; // 이메일
}
