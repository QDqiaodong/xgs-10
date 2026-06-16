package com.nostalgia.controller;

import com.nostalgia.dto.EraTimelineDTO;
import com.nostalgia.entity.Era;
import com.nostalgia.service.EraService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/eras")
@RequiredArgsConstructor
public class EraController {

    private final EraService eraService;

    @GetMapping
    public ResponseEntity<List<Era>> getAllEras() {
        return ResponseEntity.ok(eraService.getAllEras());
    }

    @GetMapping("/timeline")
    public ResponseEntity<List<EraTimelineDTO>> getEraTimeline() {
        return ResponseEntity.ok(eraService.getEraTimeline());
    }
}
