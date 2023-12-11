package devwooki.study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import devwooki.study.querydsl.dto.MemberSearchCondition;
import devwooki.study.querydsl.dto.MemberTeamDTO;
import devwooki.study.querydsl.dto.QMemberTeamDTO;
import devwooki.study.querydsl.entity.Member;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

import static devwooki.study.querydsl.entity.QMember.member;
import static devwooki.study.querydsl.entity.QTeam.team;

@Repository
public class MemberJpaRepository {
    // 동시성 문제 발생하는거 ㅇ아니냐?!
    // -> 정답은X, Spring은 멀티쓰레드 환경이고 싱글톤 패턴으로 생성하더라도 전역에서 사용중
    //    또한, 생성자를 보면 JPAQueryFactory는 entityManager에 의존하고 있다.
    //    => 동시성과 상관 없이 트랜잭션 단위로 분리되서 동작을 하게 된다.
    // 자세한건 orm표준 jpa 13.1 참고(?)
    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public MemberJpaRepository(EntityManager em) {
        this.em = em;
        queryFactory = new JPAQueryFactory(em);
    }

    public void save(Member member) {
        em.persist(member);
        em.flush();
        em.clear();
    }

    public Optional<Member> findById(Long id) {
        Member member = em.find(Member.class, id);
        return Optional.ofNullable(member);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m ", Member.class).getResultList();
    }

    public List<Member> findByUsername(String username) {
        return em.createQuery("select m from Member m " +
                        "where m.username = :username ", Member.class)
                .setParameter("username", username)
                .getResultList();
    }

    //queryDSL버전
    public List<Member> findAll_DSL() {
        return queryFactory.selectFrom(member).fetch();
    }

    public List<Member> findByUsername_DSL(String username) {
        return queryFactory.selectFrom(member).where(member.username.eq(username)).fetch();
    }


    //조건문 검색
    public List<MemberTeamDTO> searchBuiler(MemberSearchCondition condition) {
        System.out.println(">>>>>>>>>>" + condition.getAgeGoe());
        BooleanBuilder builder = new BooleanBuilder();
        if (StringUtils.hasText(condition.getUsername())) { // 웹에서  넘어오는 입력값이 null 혹은 ""이 존재
            builder.and(member.username.eq(condition.getUsername()));
        }
        if (StringUtils.hasText(condition.getTeamName())) {
            builder.and(team.name.eq(condition.getTeamName()));
        }
        if (condition.getAgeGoe() != null) {
            builder.and(member.age.goe(condition.getAgeGoe()));
        }
        if (condition.getAgeLoe() != null) {
            builder.and(member.age.loe(condition.getAgeLoe()));
        }


        return queryFactory.select(Projections.constructor(
                        MemberTeamDTO.class,
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(builder)
                .fetch();
    }

    public List<MemberTeamDTO> searchWhere(MemberSearchCondition condition) {
        return queryFactory
                .select(new QMemberTeamDTO(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .fetch();
    }

    //만약 반환 타입을 바꾸게 되다면?
    public List<Member> searchWhereMember(MemberSearchCondition condition) {
        return queryFactory
                .selectFrom(member)
                .leftJoin(member.team, team)
                //where의 동적쿼리를 재사용할 수 있다 개쩐다!
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .fetch();
    }

    private BooleanExpression allCondition(MemberSearchCondition condition) {
        return usernameEq(condition.getUsername())
                .and(teamNameEq(condition.getTeamName()))
                .and(ageGoe(condition.getAgeGoe()))
                .and(ageLoe(condition.getAgeLoe()));
    }


    //Composition을 위한 BooleanExpression 사요하는 것을 좀 더 권장
    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe == null ? null : member.age.loe(ageLoe);

    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe == null ? null : member.age.goe(ageGoe);
    }

    private BooleanExpression teamNameEq(String teamName) {
        return StringUtils.hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression usernameEq(String username) {
        return StringUtils.hasText(username) ? member.username.eq(username) : null;
    }
}
