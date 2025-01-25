package com.ll.restbytdd.domain.post.comment.controller;

import com.ll.restbytdd.domain.member.member.entity.Member;
import com.ll.restbytdd.domain.post.comment.dto.PostCommentDto;
import com.ll.restbytdd.domain.post.comment.entity.PostComment;
import com.ll.restbytdd.domain.post.post.entity.Post;
import com.ll.restbytdd.domain.post.post.service.PostService;
import com.ll.restbytdd.global.exceptions.ServiceException;
import com.ll.restbytdd.global.rq.Rq;
import com.ll.restbytdd.global.rsData.RsData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
public class ApiV1PostCommentController {
    private final PostService postService;
    private final Rq rq;

    @GetMapping
    @Transactional(readOnly = true)
    public List<PostCommentDto> items(
            @PathVariable long postId
    ) {
        Post post = postService.findById(postId).orElseThrow(
                () -> new ServiceException("404-2", "%d번 글은 존재하지 않습니다.".formatted(postId))
        );

        return post.getComments()
                .stream()
                .map(PostCommentDto::new)
                .toList();
    }

    @DeleteMapping("/{commentId}")
    @Transactional
    public RsData<Void> delete(
            @PathVariable long postId,
            @PathVariable long commentId
    ) {
        Member actor = rq.checkAuthentication();

        Post post = postService.findById(postId).orElseThrow(
                () -> new ServiceException("404-2", "%d번 글은 존재하지 않습니다.".formatted(postId))
        );

        PostComment postComment = post.getCommentById(commentId).orElseThrow(
                () -> new ServiceException("404-2", "%d번 댓글은 존재하지 않습니다.".formatted(commentId))
        );

        postComment.checkActorCanDelete(actor);

        post.removeComment(postComment);

        return new RsData<>(
                "200-1",
                "%d번 댓글이 삭제되었습니다.".formatted(commentId)
        );
    }


    record PostCommentModifyReqBody(
            @NotBlank
            @Length(min = 2, max = 100)
            String content
    ) {
    }

    @PutMapping("/{commentId}")
    @Transactional
    public RsData<PostCommentDto> modify(
            @PathVariable long postId,
            @PathVariable long commentId,
            @RequestBody @Valid PostCommentModifyReqBody reqBody
    ) {
        Member actor = rq.checkAuthentication();

        Post post = postService.findById(postId).orElseThrow(
                () -> new ServiceException("404-2", "%d번 글은 존재하지 않습니다.".formatted(postId))
        );

        PostComment postComment = post.getCommentById(commentId).orElseThrow(
                () -> new ServiceException("404-2", "%d번 댓글은 존재하지 않습니다.".formatted(commentId))
        );

        postComment.checkActorCanModify(actor);

        postComment.modify(reqBody.content());
        return new RsData<>(
                "200-1",
                "%d번 댓글이 수정되었습니다.".formatted(commentId),
                new PostCommentDto(postComment)
        );
    }


    record PostCommentWriteReqBody(
            @NotBlank
            @Length(min = 2, max = 100)
            String content
    ) {
    }

    @PostMapping
    @Transactional
    public RsData<PostCommentDto> write(
            @PathVariable long postId,
            @RequestBody @Valid PostCommentWriteReqBody reqBody
    ) {
        Member actor = rq.checkAuthentication();

        Post post = postService.findById(postId).orElseThrow(
                () -> new ServiceException("404-2", "%d번 글은 존재하지 않습니다.".formatted(postId))
        );

        PostComment postComment = post.addComment(
                actor,
                reqBody.content()
        );

        postService.flush();

        return new RsData<>(
                "201-1",
                "%d번 댓글이 생성되었습니다.".formatted(postComment.getId()),
                new PostCommentDto(postComment)
        );
    }
}
