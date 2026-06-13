package com.nostalgia.controller;

import com.nostalgia.entity.Comment;
import com.nostalgia.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<Comment>> getCommentsByPostId(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(postId));
    }

    @PostMapping
    public ResponseEntity<Comment> createComment(@RequestBody Comment comment) {
        if (comment.getAuthorName() == null || comment.getAuthorName().isBlank()) {
            comment.setAuthorName("匿名用户");
        }
        return ResponseEntity.ok(commentService.createComment(comment));
    }
}
