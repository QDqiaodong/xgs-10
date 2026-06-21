package com.nostalgia.service;

import com.nostalgia.entity.RestorationRecord;
import com.nostalgia.repository.RestorationRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestorationRecordService {

    private final RestorationRecordRepository restorationRecordRepository;

    public List<RestorationRecord> getByPostId(Long postId) {
        return restorationRecordRepository.findByPostIdOrderByRestorationDateAscSortOrderAsc(postId);
    }

    public RestorationRecord getById(Long id) {
        return restorationRecordRepository.findById(id).orElse(null);
    }

    @Transactional
    public RestorationRecord create(RestorationRecord record) {
        return restorationRecordRepository.save(record);
    }

    @Transactional
    public RestorationRecord update(Long id, RestorationRecord record) {
        RestorationRecord existing = restorationRecordRepository.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }
        existing.setRestorationType(record.getRestorationType());
        existing.setCustomType(record.getCustomType());
        existing.setRestorationDate(record.getRestorationDate());
        existing.setTitle(record.getTitle());
        existing.setDescription(record.getDescription());
        existing.setBeforeImage(record.getBeforeImage());
        existing.setAfterImage(record.getAfterImage());
        existing.setMaterials(record.getMaterials());
        existing.setCost(record.getCost());
        existing.setRestorer(record.getRestorer());
        existing.setNotes(record.getNotes());
        existing.setPreservationStatusBefore(record.getPreservationStatusBefore());
        existing.setPreservationStatusAfter(record.getPreservationStatusAfter());
        existing.setSortOrder(record.getSortOrder());
        return restorationRecordRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        restorationRecordRepository.deleteById(id);
    }

    @Transactional
    public void deleteByPostId(Long postId) {
        restorationRecordRepository.deleteByPostId(postId);
    }
}
