package devwooki.study.querydsl.dto;

import lombok.Data;

@Data
public class MemberSearchCondition {
    //회원명, 팀명, 나이(ageGoe, ageLoe)

    private String username;
    private String teamName;
    private Integer ageGoe; //null일 수 있으므로
    private Integer ageLoe; //null일 수 있으므로


}
