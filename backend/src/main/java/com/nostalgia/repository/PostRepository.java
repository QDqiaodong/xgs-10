package com.nostalgia.repository;

import com.nostalgia.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Post> findByCategoryIdOrderByCreatedAtDesc(Long categoryId, Pageable pageable);

    Page<Post> findByEraIdOrderByCreatedAtDesc(Long eraId, Pageable pageable);

    Page<Post> findByCategoryIdAndEraIdOrderByCreatedAtDesc(Long categoryId, Long eraId, Pageable pageable);

    List<Post> findTop10ByOrderByViewCountDesc();

    @Query("SELECT COUNT(p) FROM Post p WHERE p.eraId = :eraId")
    long countByEraId(@Param("eraId") Long eraId);

    @Query("SELECT p FROM Post p WHERE p.eraId = :eraId ORDER BY p.viewCount DESC, p.createdAt DESC")
    List<Post> findTopByEraIdOrderByViewCountDesc(@Param("eraId") Long eraId, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.categoryId = :categoryId")
    long countByCategoryId(@Param("categoryId") Long categoryId);

    @Query("SELECT p FROM Post p WHERE p.categoryId = :categoryId ORDER BY p.viewCount DESC, p.createdAt DESC")
    List<Post> findTopByCategoryIdOrderByViewCountDesc(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.categoryId = :categoryId AND p.eraId = :eraId")
    long countByCategoryIdAndEraId(@Param("categoryId") Long categoryId, @Param("eraId") Long eraId);

    @Query("SELECT p.preservationStatus, COUNT(p) FROM Post p GROUP BY p.preservationStatus")
    List<Object[]> countByPreservationStatusGrouped();

    @Query("SELECT p.categoryId, p.preservationStatus, COUNT(p) FROM Post p GROUP BY p.categoryId, p.preservationStatus")
    List<Object[]> countByCategoryAndPreservationStatusGrouped();

    @Query("SELECT p.eraId, p.preservationStatus, COUNT(p) FROM Post p GROUP BY p.eraId, p.preservationStatus")
    List<Object[]> countByEraAndPreservationStatusGrouped();

    @Query("SELECT p.eraId, COUNT(p) FROM Post p GROUP BY p.eraId")
    List<Object[]> countByEraGrouped();

    @Query("SELECT p.categoryId, COUNT(p) FROM Post p GROUP BY p.categoryId")
    List<Object[]> countByCategoryGrouped();

    @Query("SELECT p.eraId, p.categoryId, COUNT(p) FROM Post p GROUP BY p.eraId, p.categoryId")
    List<Object[]> countByEraAndCategoryGrouped();

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + 1 WHERE p.id = :id")
    void incrementCommentCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Post p SET p.favoriteCount = p.favoriteCount + 1 WHERE p.id = :id")
    void incrementFavoriteCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Post p SET p.favoriteCount = p.favoriteCount - 1 WHERE p.id = :id")
    void decrementFavoriteCount(@Param("id") Long id);

    @Query("SELECT p FROM Post p WHERE p.id IN :ids ORDER BY p.createdAt DESC")
    List<Post> findByIdInOrderByCreatedAtDesc(@Param("ids") List<Long> ids);
}
