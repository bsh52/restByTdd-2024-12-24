package com.ll.restbytdd.global.rq;

import com.ll.restbytdd.domain.member.member.entity.Member;
import com.ll.restbytdd.domain.member.member.service.MemberService;
import com.ll.restbytdd.global.exceptions.ServiceException;
import com.ll.restbytdd.standard.util.Ut;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Optional;

// Request를 추상화한 객체
// Request, Response, Cookie, Session 등을 다룬다.
@RequestScope
@Component
@RequiredArgsConstructor
public class Rq {
    private final HttpServletRequest request;
    private final MemberService memberService;

    public Member checkAuthentication() {
        String credentials = request.getHeader("Authorization");
        String apiKey = credentials == null ? "" : credentials.substring("Bearer ".length());

        if (Ut.str.isBlank(apiKey)) {
            throw new ServiceException("401-1", "api key가 필요합니다.");
        }
        Optional<Member> opActor = memberService.findByApiKey(apiKey);

        if (opActor.isEmpty()) {
            throw new ServiceException("401-1", "사용자 인증정보가 올바르지 않습니다.");
        }

        return opActor.get();
    }
}
