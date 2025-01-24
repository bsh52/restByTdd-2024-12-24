package com.ll.restbytdd.domain.post.post.controller;

import com.ll.restbytdd.domain.member.member.entity.Member;
import com.ll.restbytdd.domain.post.post.dto.PostDto;
import com.ll.restbytdd.domain.post.post.dto.PostWithContentDto;
import com.ll.restbytdd.domain.post.post.entity.Post;
import com.ll.restbytdd.domain.post.post.service.PostService;
import com.ll.restbytdd.global.rq.Rq;
import com.ll.restbytdd.global.rsData.RsData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class ApiV1PostController {
    private final PostService postService;
    private final Rq rq;

    @GetMapping
    public List<PostDto> items(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize
    ) {
        Page<Post> postPage = postService.findByListedPaged(true, page, pageSize);

        return postPage.getContent()
                .stream()
                .map(PostDto::new)
                .toList();
    }

    @GetMapping("/{id}")
    public PostWithContentDto item(@PathVariable long id) {
        Post post = postService.findById(id).get();

        if (!post.isPublished()) {
            Member actor = rq.checkAuthentication();

            post.checkActorCanRead(actor);
        }

        return new PostWithContentDto(post);
    }


    record PostWriteReqBody(
            @NotBlank
            @Length(min = 2, max = 100)
            String title,
            @NotBlank
            @Length(min = 2, max = 10000000)
            String content,
            boolean published,
            boolean listed
    ) {
    }

    @PostMapping
    public RsData<PostWithContentDto> write(@RequestBody @Valid PostWriteReqBody reqBody) {
        Member actor = rq.checkAuthentication();

        Post post = postService.write(
                actor,
                reqBody.title(),
                reqBody.content(),
                reqBody.published(),
                reqBody.listed()
        );

        return new RsData<>(
                "201-1",
                "%d번 글이 작성되었습니다.".formatted(post.getId()),
                new PostWithContentDto(post)
        );
    }


    record PostModifyReqBody(
            @NotBlank
            @Length(min = 2, max = 100)
            String title,
            @NotBlank
            @Length(min = 2, max = 10000000)
            String content,
            boolean published,
            boolean listed
    ) {
    }

    @PutMapping("/{id}")
    @Transactional
    public RsData<PostWithContentDto> modify(@PathVariable long id, @RequestBody @Valid PostModifyReqBody reqBody) {
        Member actor = rq.checkAuthentication();

        Post post = postService.findById(id).get();

        post.checkActorCanModify(actor);

        postService.modify(post, reqBody.title(), reqBody.content(), reqBody.published(), reqBody.listed());

        postService.flush();

        return new RsData<>(
                "200-1",
                "%d번 글이 수정되었습니다.".formatted(post.getId()),
                new PostWithContentDto(post)
        );
    }


    @DeleteMapping("/{id}")
    public RsData<Void> delete(@PathVariable long id) {
        Member actor = rq.checkAuthentication();

        Post post = postService.findById(id).get();

        post.checkActorCanDelete(actor);

        postService.delete(post);

        return new RsData<>(
                "200-1",
                "%d번 글이 삭제되었습니다.".formatted(post.getId())
        );
    }
}
