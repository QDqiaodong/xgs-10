package com.nostalgia.controller;

import com.nostalgia.entity.TimelineEvent;
import com.nostalgia.service.TimelineEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/timeline")
@RequiredArgsConstructor
public class TimelineEventController {

    private final TimelineEventService timelineEventService;

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<TimelineEvent>> getByPostId(@PathVariable Long postId) {
        return ResponseEntity.ok(timelineEventService.getByPostId(postId));
    }

    @PostMapping
    public ResponseEntity<TimelineEvent> create(@RequestBody TimelineEvent event) {
        return ResponseEntity.ok(timelineEventService.create(event));
    }
}
