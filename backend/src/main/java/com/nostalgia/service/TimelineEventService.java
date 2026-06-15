package com.nostalgia.service;

import com.nostalgia.entity.TimelineEvent;
import com.nostalgia.repository.TimelineEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TimelineEventService {

    private final TimelineEventRepository timelineEventRepository;

    public List<TimelineEvent> getByPostId(Long postId) {
        return timelineEventRepository.findByPostIdOrderByEventDateAscSortOrderAsc(postId);
    }

    @Transactional
    public TimelineEvent create(TimelineEvent event) {
        return timelineEventRepository.save(event);
    }

    @Transactional
    public List<TimelineEvent> createAll(List<TimelineEvent> events) {
        return timelineEventRepository.saveAll(events);
    }

    @Transactional
    public void deleteByPostId(Long postId) {
        timelineEventRepository.deleteByPostId(postId);
    }
}
