package devwooki.study.querydsl.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) //JPA 스펙에서 접근제한자 protected가지
@ToString(of = {"id", "name"}) //member도 추가되면 순환참조가 발생한다, @JsonIgnore를 통해 해결가능
public class Team {
    @Id
    @GeneratedValue
    @Column(name = "team_id")
    private Long id;

    private String name;

    @OneToMany(mappedBy = "team")
    //연관관계 주인이 아니기 때문에 외래키를 업데이트할 필요가 없다.
    private List<Member> members = new ArrayList<>();

    public Team(String name) {
        this.name = name;
    }
}