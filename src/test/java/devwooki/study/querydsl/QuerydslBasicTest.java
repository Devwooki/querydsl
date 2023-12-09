package devwooki.study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import devwooki.study.querydsl.entity.Member;
import devwooki.study.querydsl.entity.QMember;
import devwooki.study.querydsl.entity.QTeam;
import devwooki.study.querydsl.entity.Team;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLOutput;
import java.util.List;

import static devwooki.study.querydsl.entity.QMember.*;
import static devwooki.study.querydsl.entity.QTeam.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Slf4j
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
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));
    }

    @Test
    public void startJPQL() {
        String query = "select m from Member m " +
                "where m.username = :username";
        //member  1을 찾기
        Member findMember = em.createQuery(query, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();
        assertThat(findMember.getUsername()).isEqualTo("member1");
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

        assertThat(dslFindMember.getUsername()).isEqualTo("member1");
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

        assertThat(member1.getUsername()).isEqualTo("member1");
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

        assertThat(member1.getUsername()).isEqualTo("member1");
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

    /**
     * 회원 정렬 순서
     * 1. 나이 desc
     * 2. 이름 asc
     * 단, 2에서 이름이 없으면 마지막에 출력 -> null last
     * */
    @Test
    public void sortTest(){
        System.out.println(">>>> Test Start!! <<<<");
        List<Member> memberList = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(),
                        member.username.asc().nullsLast()).fetch();

        Member member5 = memberList.get(0);
        Member member6 = memberList.get(1);
        Member memberNull = memberList.get(2);

        System.out.println("______________________________");
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
        System.out.println("------------------------------");
    }

    @Test
    public void paging1(){
        // dialect 마다 다른 쿼리가 출력될거임
        List<Member> memberList = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1) //시작지점 입력 0 부터 시작
                .limit(2)   //2개씩 가져옴
                .fetch();

        assertThat(memberList.size()).isEqualTo(2);
    }

    @Test
    public void paging2(){
        // dialect 마다 다른 쿼리가 출력될거임
        QueryResults<Member> queryResults = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1) //시작지점 입력 0 부터 시작
                .limit(2)   //2개씩 가져옴
                .fetchResults();

        //전체 갯수
        assertThat(queryResults.getTotal()).isEqualTo(7);
        //pageSize
        assertThat(queryResults.getLimit()).isEqualTo(2);
        //Offset
        assertThat(queryResults.getOffset()).isEqualTo(1);
        //반환되는 리스트 사이즈
        assertThat(queryResults.getResults().size()).isEqualTo(2);

        //실무에서 적용 불가능할 때 있음 -> Count 쿼리를 사용해야하는 경우 -> 쿼리를 따로 써야함
        //위 같은 경우 content 쿼리라고 지칭
    }

    @Test
    public  void groupMethod(){
        List<Tuple> fetch = queryFactory
                .select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member).fetch();

        //엥? List의 제네리기 Tuple?
        // QueryDSL이 제공하는 Tuple -> 여러타입을 꺼내올  수 있는 것

        //Tuple사용법
        Tuple tuple = fetch.get(0);

        //tuple.get(QueryFactory에서 입력한 select 구문 그대로 입력)
        assertThat(tuple.get(member.count())).isEqualTo(7);
        log.info("{}",tuple.get(member.age.sum()));
        log.info("{}",tuple.get(member.age.avg()));
        log.info("{}",tuple.get(member.age.max()));
        log.info("{}",tuple.get(member.age.min()));

        //튜플쓰는  이유
        //데이터 타입이 여러개 -> Tuple사용, 실무에선 DTO로 직접 뽑아서  사요
    }

    /**
     * 팀의 이름과 각 티믜 평균 연려을 구해라
     * */
    @Test
    public void groupBy() throws Exception{
        List<Tuple> fetch = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = fetch.get(0);
        Tuple teamB = fetch.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("A팀");
        assertThat(teamB.get(team.name)).isEqualTo("B팀");
    }

    /**
     * A팀에 소속된 모든 사람을 찾아라
     * */
    @Test
    public void basicJoin() throws Exception{
        List<Member> memberList = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                //.leftJoin(member.team, team)
                //.rightJoin(member.team, team)
                .where(team.name.eq("A팀")).fetch();

        assertThat(memberList)
                .extracting("username")
                .containsExactly("member1", "member2");

        //세타조인 : 연관관계가 없어도 조인을 할 수 있음 ?
    }

    /**
     * 연습을 위한 억지 코드
     * 예제 : 멤버 이름이 팀 이름과 같은 사람 출력
     * */
    @Test
    public void thetaJoin() throws Exception{
        em.persist(new Member("A팀"));
        em.persist(new Member("B팀"));
        em.persist(new Member("C팀"));

        List<Member> memberList = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(memberList)
                .extracting("username")
                .containsExactly("A팀", "B팀");
        //주의사항 -> from절에 여러 엔티티 선택해서 세타 조인
        //외부조인 불가능 -> on을 사요하면 어느정도 해소 가능
    }

    /*
    * on을 활용하는 Join
    * 1. 조인 대상을 필터링
    * 2. 연관관계 없는 엔티티 외부 조인 <- 해당 기능을  구현할 때 on절 많이사용
    * - 카테시안곱이 발생해서
    */

    /**
     * 회원과 팀을 조인하면서, 팀 이름이 A인 팀만 조회, 회원 모두 조회
     * JPQL : select m from member m left join team t on t.name = 'teamA'
     * */
    @Test
    public void 모든_멤버를_조회할_때_팀_이름이_A인것만() throws Exception{
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("A팀")) // join조건이 team.name == 'A팀'
                /*
                on  t1_0.team_id=m1_0.team_id
                    and t1_0.name=?
                */
                .fetch();

        for(Tuple tp : result){
            System.out.println("tp = " + tp);
        }
        //inner join에서 on절 -> where절로쓰는게 더 좋다
    }

    @Test
    public void 연관관계_없는_엔티티_외부조인() throws Exception{
        // 회원 이름이 팀 이름과 같은 대상  외부조인
        em.persist(new Member("A팀"));
        em.persist(new Member("B팀"));
        em.persist(new Member("C팀"));

        List<Tuple> fetch = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();
        //멤버이름과 팀이름이 같은걸 세타조인하기 때문에 leftJoin에 team을 넣는다.
        //기존에는 member.teamId = team.Id였다면
        //이번에는 ID가 아니라 그냥 on절로 된다.
        // left join team t1_0 on m1_0.username=t1_0.name

        for(Tuple tp : fetch){
            System.out.println("tp = " + tp);
        }
        //주의사항 -> from절에 여러 엔티티 선택해서 세타 조인
        //외부조인 불가능 -> on을 사요하면 어느정도 해소 가능
    }

    /**
     * 연관된 엔티티를 SQL로 한 번에 조회하는 기능 -> 성능 최적화*/
    @PersistenceUnit //entityManager를 만드는 Factory가 잇음
    EntityManagerFactory emf;
    @Test
    public void 페치조인_X() throws Exception{
        //영속성 컨텍스트 초기화
        em.flush();
        em.clear();

        //지연로딩으로 member, team SQL쿼리 각각실행
        //현재 member엔티티에서 team은 LAZY선언 했기에 member만 조회됨
        Member member1 = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        //member1의 team이 로딩 되었는지 체크
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(member1.getTeam());
        assertThat(loaded).as("패치 조인 미적용으로 team이 로딩되지 않음").isFalse();
        //as : description
    }
    @Test
    public void 페치조인_O() throws Exception{
        //영속성 컨텍스트 초기화
        em.flush();
        em.clear();

        //지연로딩으로 member, team SQL쿼리 각각실행
        //현재 member엔티티에서 team은 LAZY선언 했기에 member만 조회됨
        Member member1 = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin() // <<- fetchJoin만 적어주면 끝
                .where(member.username.eq("member1"))
                .fetchOne();

        //member1의 team이 로딩 되었는지 체크
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(member1.getTeam());
        assertThat(loaded).as("패치 조인 적용으로 team이 로딩됨").isTrue();
        //as : description
    }

    /**
     * 나이가 가장 많은 회원 조회
     */
    @Test //subQuery : com.querydsl.jpa.JPAExpressions 패키지를 추가하면된다.
    public void 서브쿼리_나이가_가장_많은_회원_조회() throws Exception{
        //서브쿼리기 때문에 alias가 겹치면 안된다. -> Qmember를 하나 만들엉야한다.
        QMember memberSub = new QMember("memberSub");
        List<Member> memberList = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub)
                )).fetch();

        assertThat(memberList).extracting("age").contains(100);
    }

    @Test //subQuery : com.querydsl.jpa.JPAExpressions 패키지를 추가하면된다.
    public void 서브쿼리_나이가_평균_이상인_멤버() throws Exception{
        //서브쿼리기 때문에 alias가 겹치면 안된다. -> Qmember를 하나 만들엉야한다.
        QMember memberSub = new QMember("memberSub");

        List<Member> memberList = queryFactory
                .selectFrom(member)
                .where(member.age.goe( //greate or equal
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                )).fetch();

        assertThat(memberList).extracting("age").containsExactly(100);
    }

    @Test //subQuery : com.querydsl.jpa.JPAExpressions 패키지를 추가하면된다.
    public void 서브쿼리_나이가_포함되는_멤버_억지성_예제() throws Exception{
        //서브쿼리기 때문에 alias가 겹치면 안된다. -> Qmember를 하나 만들엉야한다.
        QMember memberSub = new QMember("memberSub");

        List<Member> memberList = queryFactory
                .selectFrom(member)
                .where(member.age.in( //greate or equal
                        JPAExpressions
                                .select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(20))
                )).fetch();

        assertThat(memberList).extracting("age").containsExactly(100);
    }

    @Test //subQuery : com.querydsl.jpa.JPAExpressions 패키지를 추가하면된다.
    public void 서브쿼리_SELECT절() throws Exception{
        //서브쿼리기 때문에 alias가 겹치면 안된다. -> Qmember를 하나 만들엉야한다.
        QMember memberSub = new QMember("memberSub");

        List<Tuple> tuples = queryFactory
                .select(member.username,
                        JPAExpressions //이것도 static import하면 select만으로도 쓸 수 있다..
                                .select(memberSub.age.avg())
                                .from(memberSub))
                .from(member)
                .fetch();
        
        for(Tuple tp : tuples){
            System.out.println("tp = " + tp);
        }
        /**
         * JPA이용시, 서브쿼리의 한계가 반드시 존재함.
         * 인라인뷰(From절 서브쿼리)는 지원하지 않는다 -> QueryDSL도 지원안함
         *
         * 해결법(우선순위)
         * 1. Join으로 변경 -> 간혹 안되는 경우 주의
         * 2. 쿼리를 2번 나눠서 실행
         * 3. nativeQuery 사용
         *
         * from절 서브쿼리는 굉장히 안좋다.. 화면에 맞추려고 하지말고 서버에서 로직처리를 해라!
         * 그룹핑, 필터링을 잘하라는 말
         * SQL : 데이터 가져오는데 집중
         * 서버 : 로직처리가 가장 중요
         * 화면 : 렌더링
         *
         * 실시간 유저 화면 : 빠른 대응
         * 어드민 : 느려도 ㄱㅊ -> 한 번에 가졍오려고 온몸 비틀기보다 그냐 쿼리 2~3번 날리는게..
         * */
    }

    @Test // 흔히 쓰는 case when then 적용
    public void case_문() throws Exception{
        List<String> list = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("노인"))
                .from(member)
                .fetch();
        
        for(String str : list){
            System.out.println("str = " + str);
        }
    }

    @Test // 흔히 쓰는 case when then 적용
    //DB데이터의 변경은 서비스 로직단에서 수해하자 -> 효율이 별로
    public void case_복잡한_조건() throws Exception{
        List<String> list = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0,10)).then("유아기")
                        .when(member.age.between(11,20)).then("청년기")
                        .when(member.age.between(21,40)).then("MZ")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for(String str : list){
            System.out.println("str = " + str);
        }
    }

    @Test
    public void 상수처리() throws Exception{
        List<Tuple> fetch = queryFactory
                //JPQL에서 처리하는게 아니라 가져온 값을 처리한다.
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();
        for(Tuple tp :fetch)
            System.out.println("tp = " + tp);
    }

    @Test
    public void concat() throws Exception{
        List<String> list = queryFactory
                //{username}_{age}형태로 만들고 싶다 -> 근데 서로의 타입이 다르니 맞춰주자
                //stringValue를 적용하니 JPQL에소 char로 형변환을 수행함.
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .fetch();
        for(String str : list)
            System.out.println("str = " + str);
        //문자가 아닌 타입을 변환할 수 있음. 특히, ENUM을 처리하는데 용이하다.
    }
}


