package com.nostalgia.controller;

import com.nostalgia.entity.Favorite;
import com.nostalgia.entity.Post;
import com.nostalgia.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping("/check/{postId}")
    public ResponseEntity<Map<String, Boolean>> checkFavorite(
            @PathVariable Long postId,
            @RequestParam String userSession) {
        boolean favorited = favoriteService.isFavorited(postId, userSession);
        return ResponseEntity.ok(Map.of("favorited", favorited));
    }

    @PostMapping("/toggle/{postId}")
    public ResponseEntity<Map<String, Object>> toggleFavorite(
            @PathVariable Long postId,
            @RequestBody Map<String, String> body) {
        String userSession = body.get("userSession");
        Favorite result = favoriteService.toggleFavorite(postId, userSession);
        return ResponseEntity.ok(Map.of(
                "favorited", result != null,
                "favorite", result
        ));
    }

    @GetMapping("/user")
    public ResponseEntity<List<Post>> getUserFavorites(@RequestParam String userSession) {
        return ResponseEntity.ok(favoriteService.getUserFavorites(userSession));
    }
}
