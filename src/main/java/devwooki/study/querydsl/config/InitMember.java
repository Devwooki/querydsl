package devwooki.study.querydsl.config;

import devwooki.study.querydsl.entity.Member;
import devwooki.study.querydsl.entity.Team;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Profile("local")
@Component // bean에 자동등록이 되도록
@RequiredArgsConstructor
public class InitMember {

    private final InitMemberService initMemberService;

    @PostConstruct
    public void init(){
        initMemberService.init();
    }

    @Component
    static class InitMemberService{
        @PersistenceContext
        private EntityManager em;
        
        @Transactional
        public void init(){
            //질문, 이왕하는거 PostConstructor에 넣음 안되냐?
            // ㅇㅇㅇ -> Spring lifecycle에 의해 안댄다
            //       -> PostConstructor와 Transactional을 함께 못씀. 분리시켜야함
            Team teamA = new Team("A팀");
            Team teamB = new Team("B팀");
            em.persist(teamA);
            em.persist(teamB);


            for(int i = 0 ; i < 100 ; ++i){
                Team selectedTeam = i%2 == 0 ? teamA : teamB;
                em.persist(new Member("member" + i, i));
            }
        }
    }
}
