package com.example.coffeeproject.User.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "관리자 ID는 필수입니다.")
    @Pattern(regexp = "[a-zA-Z0-9]{4,9}", message = "관리자 ID는 영어와 숫자 조합 4-9자리여야 합니다.") // 영어 + 숫자조합 4자리 ~ 9자리
    private String admin_id; // 아이디

    @NotNull(message = "비밀번호는 필수입니다.")
    @Pattern(regexp ="(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,16}", message = "비밀번호는 특수문자, 숫자, 영문을 포함한 8-16자리여야 합니다.") // 특수문자 포함 8자리~16자리
    private String admin_pw; // 비밀번호

    private String name; // 이름

    private String phone; // 핸드폰
    private String address; // 주소
    private String age; // 나이
    private String email; // 이메일
}
