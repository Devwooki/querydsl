package devwooki.study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import devwooki.study.querydsl.entity.Member;
import devwooki.study.querydsl.entity.Team;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Slf4j
public class QuerydslJuniorTest {
    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach //테스트 수행 전 데이터를 미리 입력하는 코드
    public void before() {
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("A팀");
        Team teamB = new Team("B팀");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));
    }

    //프로젝션과 결과 반환 - 기본
    // 프로젝션 : select 대상을 지정한 것
    //  대상이 1개 -> 타입을 명확하게 지정가능
    //  대상이 2개 이상 -> 튜플이나 DTO로 조회
    //   - Tuple : QueryDSL에서 여러 타입을 반환받을것을 고려해 만들어진 타입
}
