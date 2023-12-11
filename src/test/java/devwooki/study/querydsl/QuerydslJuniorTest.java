package devwooki.study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import devwooki.study.querydsl.dto.MemberDTO;
import devwooki.study.querydsl.dto.QMemberDTO;
import devwooki.study.querydsl.dto.UserDTO;
import devwooki.study.querydsl.entity.Member;
import devwooki.study.querydsl.entity.QMember;
import devwooki.study.querydsl.entity.Team;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static devwooki.study.querydsl.entity.QMember.member;
import static org.assertj.core.api.Assertions.assertThat;


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
    //   => 결국 튜플의 사용도 지양해야한다. Tuple은 queryDSL에 종속된 객체이기 때문. DTO 사용권장하는 이유
    
    @Test
    public void JPQL을_이용해서_DTO반환받기() throws Exception{
        //new Operation을 사용해 패키지경로.dto파일을  입력해야하며
        //생성자의 파라미터 순서에 맞춰야하는 번거로움이 있다
        //또, 생성자 방식만 지원하고 있다.
        List<MemberDTO> resultList = em.createQuery(
                "select new devwooki.study.querydsl.dto.MemberDTO(m.username, m.age)  from Member m ",
                MemberDTO.class).getResultList();
        for(MemberDTO md : resultList) System.out.println("md = " + md);
    }

    // QueryDSL 적용시 다양한 방법이 존재한다.
    // 1. 프로퍼티 접근(Setter) - 기본 생성자를 반드시 필요로함, 없으면 newInstance 에러 발생
    // 2. 필드 직접 접근
    //  - getter, setter가 없어도 된다. field에 값을 바로 떄려박아버린다.
    // 3. 생성자 사용
    //  - 생성자 타입을 맞춰줘야한다.(파라미터의 타입과 순서를 맞추면 문제X)
    @Test
    public void QueryDSL을_이용해서_DTO반환_프로퍼티() throws Exception{
        List<MemberDTO> setter = queryFactory
                .select(Projections.bean(           //프로퍼티 접근
                        //Projections.fields(       //필드 직접 접근
                        //Projections.constructor(  //생성자 사용
                        MemberDTO.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        for (MemberDTO md : setter) System.out.println("md = " + md);
    }

    //간혹 별칭이 다른 경우가 존재한다.
    @Test
    public void QueryDSL을_이용해서_DTO반환_별칭_변경() throws Exception{
        QMember memberSub = new QMember("memberSub");

        List<UserDTO> setter = queryFactory
                .select(Projections.fields(//필드로 접근
                        UserDTO.class,
                        member.username.as("name"), // <- .as를 이용해 alias를 지정해주면 된다.
                        ExpressionUtils.as(JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub), "age")))
                        //member.age))
                .from(member)
                .fetch();
        for (UserDTO ud : setter) System.out.println("ud = " + ud);
        /* 출력  결과를 보니 Member엔티티의 username이 UserDTO의 name과 달라 매핑되지 않는다
        ud = UserDTO(name=null, age=10)
        ud = UserDTO(name=null, age=20)
        ud = UserDTO(name=null, age=30)
        ud = UserDTO(name=null, age=40)
        ud = UserDTO(name=null, age=100)
        ud = UserDTO(name=null, age=100)
        ud = UserDTO(name=null, age=100)
        해결하기 위해 2가지 방법이 존재한다.
        1. 엔티티 필드.as("별칭")을 지정해 DTO의 필드와 매핑시켜준다.
        2. SubQuery를 이용해 별칭 지정ㅇ
            -> ExpressionUtils.as(JPAExpresstions.select(memberSub.age.max()).from(memberSub), "age")
        */
    }

    //프로젝션 결과 반환 @QueryProjection
    // 1. DTO의 생성자에 어노테이션을 적용한다.
    // 2. Gradle의 compileQueryDSL 적용 -> DTO가 Q타입으로 생성됨
    @Test
    public void DTO를_QueryProjection_적용해보자() throws Exception{
        List<MemberDTO> list = queryFactory
                .select(new QMemberDTO(member.username, member.age))
                //.select(new QMemberDTO(member.username, member.age, member.id)) <- 에러가 발생한다.
                .from(member)
                .fetch();
        for (MemberDTO memberDTO : list) {
            System.out.println("memberDTO = " + memberDTO);
        }
        // Projections.constructor(생성자 사용)와 무슨차이가 있는가?
        // -> 생성자 사용은 필드가 추가되면 런타임 에러가 발생한다
        // -> QueryProjection에는 필드 추가시 컴파일 에러 발새

        // 컴파일 시점에 에러가 발생하므로 좋지만 단점이 명확하다
        // 1. Q객체를 생성해야함
        // 2. DTO는 QueryDSL에 종속적  -> 아키텍쳐적으로 애매한 부분
        // 3. DTO가
    }

    //동적쿼리
    // 1. BooleanBuilder
    // 2. Where다중 파라미터사용
    @Test
    public void 동적쿼리_booleanBuilder() throws Exception{
        String usernameParam = "member1";
        Integer ageParam = null;

        //파라미터의 값이 null이냐 아니냐에 따라 쿼리가 동적으로 변해야한다.
        // 즉 ageParam이 null이면 usernameParam만 처리한다.
        List<Member> result = searchMember1(usernameParam, ageParam);

        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameParam, Integer ageParam) {
        //초기조건을 생성할 때 추가하면 기본 조건으로 설정된다.
        BooleanBuilder builder = new BooleanBuilder(member.age.eq(ageParam));
        //BooleanBuilder builder = new BooleanBuilder();

        //where절에 들어갈 조건문을 처리한다. null이면 조건을 추가하지 않도록 작성하는 쿼리
        if(usernameParam != null) builder.and(member.username.eq(usernameParam));
        if(ageParam != null) builder.and(member.age.eq(ageParam));

        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }
    
    @Test // 김영한님이 실무에서 사용하는 권장하는 방법
    public void 동적쿼리_Where_다중파라미터() throws Exception{
        String usernameParam = "member1";
        Integer ageParam = null;

        List<Member> result = searchMember2(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember2(String usernameCond, Integer ageCond) {
        return queryFactory
                .selectFrom(member)
                //where절에 사용하는 메소드의 반환값이 null이면 해당 조건은 무시가 된다
                //.where(usernameEq(usernameCond), ageEq(ageCond))
                .where(allEQ(usernameCond, ageCond)) //allEq로 한방에 조립해서 사용하는 BooleanBuilder형태로도 가능
                .fetch();
    }

    private BooleanExpression usernameEq(String usernameCond) {
        return usernameCond != null ? member.username.eq(usernameCond) : null;
    }

    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond)  : null;
    }
    /* 이런걸 언제쓰냐
        1. 광고상태 isValid and 날짜 in -> 광고상태를 관리하는 isServiceable조건을 만들어서 사용
        한번 조합을 만들어 놓으면 재사용이 가능하기 때문
        * */
    private BooleanExpression allEQ(String usernameParam, Integer ageParam){
        return usernameEq(usernameParam).and(ageEq(ageParam));
    }


    //수정, 삭제 배치쿼리 -> Bulk연산 쿼리 한 번으로 대량의 데이터 수저하는 작업
    // JPAㅔ선 Transaction commit할 때 일어나기에 "Dirty checking" 혹은 "변경 감지"발생

    @Test
    //@Commit //@SpringBootTest에서 @Transaction을 하게 되면 테스트 코드이기 때문에 수행후 rollback해버린다.
    //결과 조회를 위해 commit을 적용함
    public void 벌크_업데이트() throws Exception{
        queryFactory
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(28))
                .execute();
        //주의사항 JPA의 영속성 컨텍스트
        // Bulk연산시, DB는 반영이 되지만 영속성컨텍스트는 변경되지 않았다.  -> 데이터 영속성 깨짐
        // 이후 select로 DB에서 데이터를 가져와도 영속성 컨텍스트에 데이터가 있기 때문에 컨텍스트의 업데이트가 안됨
        // 영속성 컨텍스트가 우선순위를 가짐 -> 벌크연산 후 flush, clear를 통해 초기화 시키자!
        em.flush();
        em.clear();
    }

    @Test
    public void 벌크연산_추가() throws Exception{
        queryFactory
                .update(member)
//                .set(member.age, member.age.add(1))  //더하기
                .set(member.age, member.age.add(-12)) //뺴기1
                //.set(member.age, member.age.subtract(-12)) //뺴기2
                //.set(member.age, member.age.multiply(2)) //곱하기
                //.set(member.age, member.age.divide(2)) //나누기
                //.set(member.age, member.age.mod(2)) //모듈연산
                //이외에도 sum 등 다양한 연산 방법  존재
                .execute();
        em.flush();
        em.clear();
    }

    @Test
    public void 벌크연산_delete() throws Exception{
        queryFactory
                .delete(member)
                .where(member.age.gt(28))
                .execute();
    }

    //SQL function  호출하기
    //Dialect 등록된 메소드만 호출할 수 있다.
    @Test
    public void sqlFuction_replace() throws Exception{
        List<String> list = queryFactory
                .select(Expressions.stringTemplate(//숫자면 numbertemplate 등이 있음
                        "function('replace', {0}, {1}, {2})",
                        member.username, "member", "M"))
                .from(member)
                .fetch();
        for (String s : list) {
            System.out.println("s = " + s);
        }
    }
    
    @Test
    public void sqlFunction_toUpperCase() throws Exception{
        List<String> list = queryFactory
                .select(member.username)
                .from(member)
//                .where(member.username.eq(Expressions.stringTemplate("function('upper', {0})", member.username)))
                .where(member.username.eq(member.username.upper()))
                .fetch();
        for (String s : list) {
            System.out.println("s = " + s);
        }
    }
}
