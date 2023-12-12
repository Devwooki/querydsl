package devwooki.study.querydsl.controller;

import devwooki.study.querydsl.dto.MemberSearchCondition;
import devwooki.study.querydsl.repository.MemberRepository;
import devwooki.study.querydsl.repository.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class testController {
    private final MemberJpaRepository memberJpaRepository;
    private final MemberRepository memberRepository;

    @GetMapping("/v1/members")
    public ResponseEntity<?> searchMemberV1(MemberSearchCondition condition){
        return ResponseEntity.ok(memberJpaRepository.searchWhere(condition));
    }

    @GetMapping("/v1/page/member")
    public ResponseEntity<?> searchMemberV1(MemberSearchCondition condition, Pageable pageable){
        return ResponseEntity.ok(memberRepository.searchPageComplex2(condition, pageable));
    }
}
