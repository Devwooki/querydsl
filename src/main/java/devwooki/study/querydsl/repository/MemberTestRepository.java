package devwooki.study.querydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import devwooki.study.querydsl.config.Querydsl4RepositorySupport;
import devwooki.study.querydsl.dto.MemberSearchCondition;
import devwooki.study.querydsl.entity.Member;
import devwooki.study.querydsl.entity.QMember;
import devwooki.study.querydsl.entity.QTeam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static devwooki.study.querydsl.entity.QMember.*;
import static devwooki.study.querydsl.entity.QTeam.*;

public class MemberTestRepository extends Querydsl4RepositorySupport {
    public MemberTestRepository(Class<?> domainClass) {
        super(domainClass);
    }

    public List<Member> basicSelect() {
        return select(member)
                .from(member)
                .fetch();
    }

    public List<Member> basicSelectFrom() {
        return selectFrom(member).fetch();
    }

    // 기존방식
    public Page<Member> searchPageApplyPage(MemberSearchCondition condition, Pageable pageable) {
        JPAQuery<Member> query = selectFrom(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                );
        List<Member> content = getQuerydsl().applyPagination(pageable, query).fetch();
        return PageableExecutionUtils.getPage(content, pageable, query::fetchCount);
    }

    // QueryDSL4Support 활용
    public Page<Member> applyPagenation(MemberSearchCondition condition, Pageable pageable) {
        return applyPagination(pageable, query ->
                query.selectFrom(member)
                        .leftJoin(member.team, team)
                        .where(usernameEq(condition.getUsername()),
                                teamNameEq(condition.getTeamName()),
                                ageGoe(condition.getAgeGoe()),
                                ageLoe(condition.getAgeLoe())
                        )
        );
    }

    public Page<Member> divideCountQuery(MemberSearchCondition condition, Pageable pageable) {
        return applyPagination(pageable,
                contentQuery ->
                        contentQuery.selectFrom(member)
                                .leftJoin(member.team, team)
                                .where(usernameEq(condition.getUsername()),
                                        teamNameEq(condition.getTeamName()),
                                        ageGoe(condition.getAgeGoe()),
                                        ageLoe(condition.getAgeLoe())
                                ),
                countQuery ->
                        countQuery.select(member.count())
                                .from(member)
        );
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
