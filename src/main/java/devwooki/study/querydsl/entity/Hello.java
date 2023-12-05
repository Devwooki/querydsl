package devwooki.study.querydsl.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Hello {
    @Id
    @GeneratedValue
    private Long id;
}
/*
 * Gradle로 QueryDSL 설정을 마쳤으면 gradle - tasks - other - compileQuerydsl을 실행한다
 * build - generated - 패키지경로 - entity 에 Q객체가 생성된 것을 확인할 수 있다.
 * Q객체 : QueryDSL에서 사용하는 객체
 * Q객체는 : git에 올라가지 않게 주의하자 -> build폴더를 ignore화
 * */
