package com.nostalgia.repository;

import com.nostalgia.entity.RestorationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestorationRecordRepository extends JpaRepository<RestorationRecord, Long> {

    List<RestorationRecord> findByPostIdOrderByRestorationDateAscSortOrderAsc(Long postId);

    void deleteByPostId(Long postId);
}
