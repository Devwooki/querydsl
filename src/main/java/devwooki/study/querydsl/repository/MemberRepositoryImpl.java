package devwooki.study.querydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import devwooki.study.querydsl.dto.MemberSearchCondition;
import devwooki.study.querydsl.dto.MemberTeamDTO;
import devwooki.study.querydsl.dto.QMemberTeamDTO;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.support.PageableUtils;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static devwooki.study.querydsl.entity.QMember.member;
import static devwooki.study.querydsl.entity.QTeam.team;

public class MemberRepositoryImpl implements MemberRepositroyCustom {

    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<MemberTeamDTO> search(MemberSearchCondition condition) {
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

    @Override
    public Page<MemberTeamDTO> searchPage(MemberSearchCondition condition, Pageable pageable) {
        QueryResults<MemberTeamDTO> results = queryFactory
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
                .offset(pageable.getOffset())   //페이지 시작점
                .limit(pageable.getPageSize())  //페이지 크기
                .fetchResults();
                //fetchResult -> contents용 쿼리와 count용 쿼리 2개를 날린 객체를 반환함

        List<MemberTeamDTO> contents = results.getResults();
        long total = results.getTotal();

        //page구현체를 반환해준다.
        return new PageImpl<>(contents, pageable, total);
    }

    @Override
    public Page<MemberTeamDTO> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDTO> contents = queryFactory
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
                .offset(pageable.getOffset())   //페이지 시작점
                .limit(pageable.getPageSize())  //페이지 크기
                .fetch();

        Long total = queryFactory.select(new QMemberTeamDTO(
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
                        ageLoe(condition.getAgeLoe())).fetchCount();
        //fetchResult를 이용하지 않고 쿼리 2번을 직접 날려서 처리한다.
        //어떤 장점이 있는가? 상황에 따라 다르지만 최적화 가능
        // Content는 복잡하고, count쿼리만이 단순할 때 -> join을2번하면 부하 더 크니까 최적화 용도로
        // 혹은 count쿼리를 먼저 수행시킨 뒤, 결과가 0이면 content쿼리를 수행하지 않도록할 수 있다.
        return new PageImpl<>(contents, pageable, total);
    }

    public Page<MemberTeamDTO> searchPageComplex2(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDTO> contents = queryFactory
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
                .offset(pageable.getOffset())   //페이지 시작점
                .limit(pageable.getPageSize())  //페이지 크기
                .fetch();

        JPAQuery<MemberTeamDTO> countQuery = queryFactory.select(new QMemberTeamDTO(
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
                        ageLoe(condition.getAgeLoe()));

        //CountQuery를 생략하는 방법을 통해 최적화 해보자
        // 생략가능한 경우
        //  1. 페이지 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을 때
        //  2. 마지막 페이지일때(offset + 컨텐츠 사이즈 < 전체 사이즈)
        return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchCount);
        //return PageableExecutionUtils.getPage(contents, pageable, () -> countQuery.fetchCount());
    }

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
