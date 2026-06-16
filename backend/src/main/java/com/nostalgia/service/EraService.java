package com.nostalgia.service;

import com.nostalgia.dto.EraTimelineDTO;
import com.nostalgia.entity.Era;
import com.nostalgia.entity.Post;
import com.nostalgia.repository.EraRepository;
import com.nostalgia.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EraService {

    private final EraRepository eraRepository;
    private final PostRepository postRepository;
    private final PostService postService;

    @Cacheable(value = "eras", key = "'all'")
    public List<Era> getAllEras() {
        return eraRepository.findAllByOrderBySortOrderAsc();
    }

    @Cacheable(value = "eras", key = "'timeline'")
    public List<EraTimelineDTO> getEraTimeline() {
        List<Era> eras = eraRepository.findAllByOrderBySortOrderAsc();
        List<EraTimelineDTO> timeline = new ArrayList<>();

        for (Era era : eras) {
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

            timeline.add(dto);
        }

        return timeline;
    }
}
