package com.nostalgia.service;

import com.nostalgia.dto.EraTimelineDTO;
import com.nostalgia.entity.Category;
import com.nostalgia.entity.Era;
import com.nostalgia.entity.Post;
import com.nostalgia.entity.PreservationStatus;
import com.nostalgia.repository.CategoryRepository;
import com.nostalgia.repository.EraRepository;
import com.nostalgia.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EraService {

    private final EraRepository eraRepository;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final PostService postService;

    @Cacheable(value = "eras", key = "'all'")
    public List<Era> getAllEras() {
        return eraRepository.findAllByOrderBySortOrderAsc();
    }

    @Cacheable(value = "eras", key = "'timeline'")
    public List<EraTimelineDTO> getEraTimeline() {
        List<Era> eras = eraRepository.findAllByOrderBySortOrderAsc();
        List<Category> categories = categoryRepository.findAllByOrderBySortOrderAsc();
        List<PreservationStatus> statuses = PreservationStatus.getAllStatuses();
        List<EraTimelineDTO> timeline = new ArrayList<>();

        for (Era era : eras) {
            timeline.add(buildEraTimelineDTO(era, categories, statuses));
        }

        return timeline;
    }

    @Cacheable(value = "eras", key = "'detail:' + #id")
    public EraTimelineDTO getEraDetail(Long id) {
        Era era = eraRepository.findById(id).orElse(null);
        if (era == null) {
            return null;
        }
        List<Category> categories = categoryRepository.findAllByOrderBySortOrderAsc();
        List<PreservationStatus> statuses = PreservationStatus.getAllStatuses();
        return buildEraTimelineDTO(era, categories, statuses);
    }

    private EraTimelineDTO buildEraTimelineDTO(Era era, List<Category> categories, List<PreservationStatus> statuses) {
        EraTimelineDTO dto = new EraTimelineDTO();
        dto.setId(era.getId());
        dto.setName(era.getName());
        dto.setYearStart(era.getYearStart());
        dto.setYearEnd(era.getYearEnd());
        dto.setDescription(era.getDescription());
        dto.setIcon(era.getIcon());
        dto.setColorScheme(era.getColorScheme());
        dto.setRepresentativeCategories(era.getRepresentativeCategories());

        long count = postRepository.countByEraId(era.getId());
        dto.setPostCount((int) count);

        List<Post> topPosts = postRepository.findTopByEraIdOrderByViewCountDesc(era.getId(), PageRequest.of(0, 3));
        topPosts.forEach(postService::populateCategoryAndEraNamesExternal);
        dto.setRepresentativePosts(topPosts);

        Map<String, Long> byCategory = new LinkedHashMap<>();
        for (Category cat : categories) {
            byCategory.put(cat.getName(), postRepository.countByCategoryIdAndEraId(cat.getId(), era.getId()));
        }
        dto.setByCategory(byCategory);

        Map<String, Long> byPreservationStatus = new LinkedHashMap<>();
        for (PreservationStatus status : statuses) {
            byPreservationStatus.put(status.getLabel(),
                    postRepository.countByEraIdAndPreservationStatus(era.getId(), status.getLabel()));
        }
        dto.setByPreservationStatus(byPreservationStatus);

        return dto;
    }
}
