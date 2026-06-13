package com.nostalgia.service;

import com.nostalgia.entity.Category;
import com.nostalgia.entity.Era;
import com.nostalgia.entity.Favorite;
import com.nostalgia.entity.Post;
import com.nostalgia.repository.CategoryRepository;
import com.nostalgia.repository.EraRepository;
import com.nostalgia.repository.FavoriteRepository;
import com.nostalgia.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final EraRepository eraRepository;

    public boolean isFavorited(Long postId, String userSession) {
        return favoriteRepository.findByPostIdAndUserSession(postId, userSession).isPresent();
    }

    @Transactional
    public Favorite toggleFavorite(Long postId, String userSession) {
        Optional<Favorite> existing = favoriteRepository.findByPostIdAndUserSession(postId, userSession);
        
        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            postRepository.decrementFavoriteCount(postId);
            return null;
        } else {
            Favorite favorite = new Favorite();
            favorite.setPostId(postId);
            favorite.setUserSession(userSession);
            Favorite saved = favoriteRepository.save(favorite);
            postRepository.incrementFavoriteCount(postId);
            return saved;
        }
    }

    public List<Post> getUserFavorites(String userSession) {
        List<Favorite> favorites = favoriteRepository.findByUserSessionOrderByCreatedAtDesc(userSession);
        List<Long> postIds = favorites.stream().map(Favorite::getPostId).collect(Collectors.toList());
        if (postIds.isEmpty()) {
            return List.of();
        }
        List<Post> posts = postRepository.findByIdInOrderByCreatedAtDesc(postIds);
        posts.forEach(post -> {
            if (post.getCategoryId() != null) {
                categoryRepository.findById(post.getCategoryId())
                    .map(Category::getName)
                    .ifPresent(post::setCategoryName);
            }
            if (post.getEraId() != null) {
                eraRepository.findById(post.getEraId())
                    .map(Era::getName)
                    .ifPresent(post::setEraName);
            }
        });
        return posts;
    }
}
