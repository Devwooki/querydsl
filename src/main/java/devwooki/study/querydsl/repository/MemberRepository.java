package devwooki.study.querydsl.repository;

import devwooki.study.querydsl.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositroyCustom {
    List<Member> findByUsername(String username);
    //MemberRepositroyCustom는 QueryDSL을 이용한 인터페이스이고 구현체가 존재한다
    //QueryDSL인터페이스를 상속받음으로써 외부에서 메소드를 불러온다
}
