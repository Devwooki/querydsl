package devwooki.study.querydsl.repository;

import devwooki.study.querydsl.dto.MemberSearchCondition;
import devwooki.study.querydsl.dto.MemberTeamDTO;

import java.util.List;

public interface MemberRepositroyCustom {
    //QueryDSL을 사용하기 위해 Custom에다가 다 때려박는건 또 좋지 않다.
    //한 조회기능이 너무 복잡할 경우 별도의 Repository로 분리해서 의존성 주입하는것도 ㄱㅊ
    // 즉 공용성 없이 특정 API에만 종속되어 있다면 분리해도 ㄱㅊ
    List<MemberTeamDTO> search(MemberSearchCondition condition);
}
