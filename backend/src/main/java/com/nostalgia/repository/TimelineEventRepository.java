package com.nostalgia.repository;

import com.nostalgia.entity.TimelineEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimelineEventRepository extends JpaRepository<TimelineEvent, Long> {

    List<TimelineEvent> findByPostIdOrderByEventDateAscSortOrderAsc(Long postId);

    void deleteByPostId(Long postId);
}
