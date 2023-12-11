package devwooki.study.querydsl.repository;

import devwooki.study.querydsl.dto.MemberSearchCondition;
import devwooki.study.querydsl.dto.MemberTeamDTO;
import devwooki.study.querydsl.entity.Member;
import devwooki.study.querydsl.entity.Team;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class DataRepositoryTest {

    @Autowired
    EntityManager em;
    @Autowired
    MemberRepository dataRepository;
    @Test
    public void basicTest() throws Exception{
        Member member = new Member("member1", 10);
        dataRepository.save(member);

        Member findMember = dataRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        //dsl 버전
        List<Member> result1 = dataRepository.findAll();
        assertThat(result1).containsExactly(member);

        List<Member> result2 = dataRepository.findByUsername("member1");
        assertThat(result2).containsExactly(member);
    }

    //Spring Data JPA는 Interface -> QueryDSL 같은 경우 사용자가 직접 구현을해야한다.
    // 사용자 정의 인터페이스 작성
    // 사용자 정의 인터펭이스 구현
    // 스프링 데이터 레포지토리에 사용자 정의 인터페이스 상속


    @Test
    public void spring_data_JPA_QueryDSL_사용() throws Exception{
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

        List<MemberTeamDTO> search = dataRepository.search(condition);
        Assertions.assertThat(search).extracting("username").containsExactly("member4");
    }
}