package devwooki.study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemberDTO {
    private String username;
    private int age;

    @QueryProjection
    public MemberDTO(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
