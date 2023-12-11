package devwooki.study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
//성능 최적화는  Spring MVC 활용편 2편
public class MemberTeamDTO {
    private Long memberId;
    private String  username;
    private  int age;
    private Long teamId;
    private String teamName;

    @QueryProjection
    public MemberTeamDTO(Long memberId, String username, int age, Long teamId, String teamName) {
        this.memberId = memberId;
        this.username = username;
        this.age = age;
        this.teamId = teamId;
        this.teamName = teamName;
    }
}
