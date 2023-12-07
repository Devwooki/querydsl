package devwooki.study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import devwooki.study.querydsl.entity.Member;
import devwooki.study.querydsl.entity.QMember;
import devwooki.study.querydsl.entity.Team;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static devwooki.study.querydsl.entity.QMember.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {
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
    }

    @Test
    public void startJPQL() {
        String query = "select m from Member m " +
                "where m.username = :username";
        //member  1을 찾기
        Member findMember = em.createQuery(query, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();
        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQueryDSL() {
        //jpa쿼리 팩토리를 생성해야함 -> 엔티티 매니저를 변수로 넘겨준다.
        // 필드로 뺸뒤 @BeforeEach에 선언 함으로써 더욱 편하게 사용가능
        // 동시성 문제도 해결해준다 했는데..? 오잉 <- 스프링이 주입하는 객체 자체가 멀티 스레드에 문제 없도록 해결해줌, 바인딩 되게 분배해줌
        //JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        // 변수명에 alias를 입력한다. 크게 중요치 않음.. 전에 Qmember.member; 했던거 처럼
        QMember qm = new QMember("m");

        //querydsl은 파라미터 바인딩이 필요없다. preparedStatement로 알아서 바인딩
        // sql injection 예방 가능
        // 뿐만 아니라 JPQL과 달리 런타임 이전에 파악을 할 수 있다는게 장점.
        Member dslFindMember = queryFactory
                .select(qm)
                .from(qm)
                .where(qm.username.eq("member1"))
                //like eq gt lt 전부 다 됨 개쩐다..
                .fetchOne();

        // q타입 객체가 너무 지저분하니 다음과 같이 바꿀 수 있음
        // import 영역에 Qmembe가 static으로 import된것을 확인할 수 있다.
        // 결국 JPQL의 빌더 역할을 수행한다. -> 궁금하면 yml에 use sql comment : true 주면 됨
        Member qmemberForStatic = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                //like eq gt lt 전부 다 됨 개쩐다..
                .fetchOne();

        Assertions.assertThat(dslFindMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void search(){
        //and or 전부 가능
        // 이외에도 무수히 많은 검색 기능이 있다.
        Member member1 = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();

        Assertions.assertThat(member1.getUsername()).isEqualTo("member1");
    }

    @Test
    public void andParam(){
        //and or 전부 가능
        // 이외에도 무수히 많은 검색 기능이 있다.
        Member member1 = queryFactory
                .selectFrom(member)
                .where(
                        //and를 , 으로 분기해도 된다.. 개쩌네 다만, ','은 and만 포함
                        // and만 있는 경우 해당 코드를 권장, null이 입력되어도 동적할당이 가능하기 때문
                        member.username.eq("member1"),
                        member.age.eq(10)
                )
                .fetchOne();

        Assertions.assertThat(member1.getUsername()).isEqualTo("member1");
    }

    @Test
    public void fetchTest(){
        //and or 전부 가능
        // 이외에도 무수히 많은 검색 기능이 있다.

        //모든결과 반환
//        List<Member> fetch = queryFactory
//                .selectFrom(member)
//                .fetch();

        //1개 반환
//        Member one = queryFactory
//                .selectFrom(member)
//                .fetchOne();
//
        //Limit 1 반환
//        Member fetchFirst = queryFactory
//                .selectFrom(member)
//                .fetchFirst();

        //여기가 이상하다.. 쿼리가 2번 실행될 예정
//        QueryResults<Member> results = queryFactory
//                .selectFrom(member)
//                .fetchResults();

        //아래 구문 별로 각각 쿼리를 요총한다.. 이상한 녀석들!
        //total은 count ㄱ갯수로 아이디만 select로 가져온다.
        //results.getTotal(); //totalCNT를 반환

        //멤버 데이터 전부 가져온다.
        //List<Member> contents = results.getResults();

        Long total = queryFactory
                .selectFrom(member)
                .fetchCount();


    }
}
