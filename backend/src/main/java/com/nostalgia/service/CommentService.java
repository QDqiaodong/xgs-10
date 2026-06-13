package com.nostalgia.service;

import com.nostalgia.entity.Comment;
import com.nostalgia.repository.CommentRepository;
import com.nostalgia.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId);
    }

    @Transactional
    public Comment createComment(Comment comment) {
        Comment saved = commentRepository.save(comment);
        postRepository.incrementCommentCount(comment.getPostId());
        return saved;
    }
}
