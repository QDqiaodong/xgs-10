package com.nostalgia.repository;

import com.nostalgia.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByPostIdAndUserSession(Long postId, String userSession);
    List<Favorite> findByUserSessionOrderByCreatedAtDesc(String userSession);
    void deleteByPostIdAndUserSession(Long postId, String userSession);
}
