package com.ll.restbytdd.domain.member.member.controller;

import com.ll.restbytdd.domain.member.member.dto.MemberDto;
import com.ll.restbytdd.domain.member.member.entity.Member;
import com.ll.restbytdd.domain.member.member.service.MemberService;
import com.ll.restbytdd.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class ApiV1MemberController {
    private final MemberService memberService;

    record MemberJoinReqBody(
            String username,
            String password,
            String nickname
    ){}

    @PostMapping("/join")
    public RsData<MemberDto> join(@RequestBody MemberJoinReqBody reqBody) {
        Member member = memberService.join(reqBody.username(), reqBody.password(), reqBody.nickname());

        return new RsData<>(
                "201-1",
                "%s님 환영합니다. 회원가입이 완료되었습니다.".formatted(member.getName()),
                new MemberDto(member)
        );
    }
}
