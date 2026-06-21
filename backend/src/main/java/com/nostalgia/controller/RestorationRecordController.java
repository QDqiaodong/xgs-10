package com.nostalgia.controller;

import com.nostalgia.entity.RestorationRecord;
import com.nostalgia.service.RestorationRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/restoration")
@RequiredArgsConstructor
public class RestorationRecordController {

    private final RestorationRecordService restorationRecordService;

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<RestorationRecord>> getByPostId(@PathVariable Long postId) {
        return ResponseEntity.ok(restorationRecordService.getByPostId(postId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestorationRecord> getById(@PathVariable Long id) {
        RestorationRecord record = restorationRecordService.getById(id);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(record);
    }

    @PostMapping
    public ResponseEntity<RestorationRecord> create(@RequestBody RestorationRecord record) {
        return ResponseEntity.ok(restorationRecordService.create(record));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestorationRecord> update(@PathVariable Long id, @RequestBody RestorationRecord record) {
        RestorationRecord updated = restorationRecordService.update(id, record);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        restorationRecordService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
