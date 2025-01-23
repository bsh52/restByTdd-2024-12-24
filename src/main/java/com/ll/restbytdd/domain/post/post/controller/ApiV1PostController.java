package com.ll.restbytdd.domain.post.post.controller;

import com.ll.restbytdd.domain.member.member.entity.Member;
import com.ll.restbytdd.domain.post.post.dto.PostDto;
import com.ll.restbytdd.domain.post.post.entity.Post;
import com.ll.restbytdd.domain.post.post.repository.PostRepository;
import com.ll.restbytdd.domain.post.post.service.PostService;
import com.ll.restbytdd.global.rq.Rq;
import com.ll.restbytdd.global.rsData.RsData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class ApiV1PostController {
    private final PostService postService;
    private final Rq rq;

    @GetMapping("/{id}")
    public PostDto item(@PathVariable long id) {
        return new PostDto(postService.findById(id).get());
    }


    record PostWriteReqBody(
            @NotBlank
            @Length(min = 2, max = 100)
            String title,
            @NotBlank
            @Length(min = 2, max = 10000000)
            String content
    ) {
    }

    @PostMapping
    public RsData<PostDto> write(@RequestBody @Valid PostWriteReqBody reqBody) {
        Member actor = rq.checkAuthentication();

        Post post = postService.write(actor, reqBody.title(), reqBody.content());

        return new RsData<>(
                "201-1",
                "%d번 글이 작성되었습니다.".formatted(post.getId()),
                new PostDto(post)
        );
    }


    record PostModifyReqBody(
            @NotBlank
            @Length(min = 2, max = 100)
            String title,
            @NotBlank
            @Length(min = 2, max = 10000000)
            String content
    ) {
    }

    @PutMapping("/{id}")
    @Transactional
    public RsData<PostDto> modify(@PathVariable long id, @RequestBody @Valid PostModifyReqBody reqBody) {
        Member actor = rq.checkAuthentication();

        Post post = postService.findById(id).get();

        post.checkActorCanModify(actor);

        postService.modify(post, reqBody.title(), reqBody.content());

        postService.flush();

        return new RsData<>(
                "200-1",
                "%d번 글이 수정되었습니다.".formatted(post.getId()),
                new PostDto(post)
        );
    }

    private final PostRepository postRepository;
}
