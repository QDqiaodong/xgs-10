package com.nostalgia.service;

import com.nostalgia.entity.Era;
import com.nostalgia.repository.EraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EraService {

    private final EraRepository eraRepository;

    @Cacheable(value = "eras", key = "'all'")
    public List<Era> getAllEras() {
        return eraRepository.findAllByOrderBySortOrderAsc();
    }
}
