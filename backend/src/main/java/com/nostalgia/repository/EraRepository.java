package com.nostalgia.repository;

import com.nostalgia.entity.Era;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EraRepository extends JpaRepository<Era, Long> {
    List<Era> findAllByOrderBySortOrderAsc();
}
