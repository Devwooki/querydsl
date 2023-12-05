package devwooki.study.querydsl.entity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Slf4j
class MemberTest {

    @Autowired
    EntityManager em;

    @Test
    public void entityTest(){
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

        //영속성 컨텍스트 초기화
        em.flush(); //영속성 컨텍스트 db에 반영
        em.clear(); //영속성 컨텍스트 초기화 후 초기화

        //조회
        List<Member> members = em.createQuery(" select m from Member m ", Member.class).getResultList();

        //확인
        for (Member member : members) {
            System.out.println("member=" + member);
            System.out.println("-> member.team= " + member.getTeam() +"\n");
        }

    }

}