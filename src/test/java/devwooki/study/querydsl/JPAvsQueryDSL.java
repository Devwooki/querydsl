package devwooki.study.querydsl;

import devwooki.study.querydsl.dto.MemberSearchCondition;
import devwooki.study.querydsl.dto.MemberTeamDTO;
import devwooki.study.querydsl.entity.Member;
import devwooki.study.querydsl.entity.Team;
import devwooki.study.querydsl.repository.MemberJpaRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Slf4j
public class JPAvsQueryDSL {
    //순수 JPA 레포지토리와 QueryDSL을 만들어보자

    @Autowired
    EntityManager em;
    @Autowired
    MemberJpaRepository jpaRepository;

    @Test
    public void basicTest() throws Exception{
        Member member = new Member("member1", 10);
        jpaRepository.save(member);

        Member findMember = jpaRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

//        List<Member> all = jpaRepository.findAll();
//        assertThat(all).containsExactly(member);
//
//        List<Member> result2 = jpaRepository.findByUsername("member1");
//        assertThat(result2).containsExactly(member);

        //dsl 버전
        List<Member> allDsl = jpaRepository.findAll_DSL();
        assertThat(allDsl).containsExactly(member);

        List<Member> byUsernameDsl = jpaRepository.findByUsername_DSL("member1");
        assertThat(byUsernameDsl).containsExactly(member);
    }

    @Test
    public void 동적쿼리와_성능최적화_조회_Builder_검색테스트() throws Exception{
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

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("B팀");
        //조건이 없으면 모든 데이터를 끌고온다. 주의
        // 기본조건을 주거나, 리미트를 설정함(페이징 쿼리)를  저굥해야한다.
        // 데이터가 3만개가 매일 쌓인다면 이것을 전부 어떻게 처리할래가 됨. 즉 테스트환경에도 부하가 커진다.

        List<MemberTeamDTO> memberTeamDTOS = jpaRepository.searchBuiler(condition);
        Assertions.assertThat(memberTeamDTOS).extracting("username").containsExactly("member4");
    }

    //2. 동적쿼리 성능 최적화, where절 파라미터 사용
    @Test
    public void 동적쿼리와_성능최적화_조회_Where_사용() throws Exception{
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

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("B팀");

        List<Member> memberTeamDTOS = jpaRepository.searchWhereMember(condition);
        Assertions.assertThat(memberTeamDTOS).extracting("username").containsExactly("member4");
    }

}