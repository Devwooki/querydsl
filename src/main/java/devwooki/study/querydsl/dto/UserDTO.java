package devwooki.study.querydsl.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserDTO {
    //member테이블의 username을 가져오지면 변수명이 다른경우도 있으니까.
    private String name;
    private int age;
}
