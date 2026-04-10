package edu.cit.caones.splitshare.controller;

import edu.cit.caones.splitshare.dto.response.ApiResponse;
import edu.cit.caones.splitshare.dto.response.UserConnectionDto;
import edu.cit.caones.splitshare.dto.response.UserActivityDto;
import edu.cit.caones.splitshare.service.UserActivityService;
import edu.cit.caones.splitshare.service.UserSocialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserSocialService userSocialService;
    private final UserActivityService userActivityService;

    @GetMapping("/me/history")
    public ResponseEntity<ApiResponse<List<UserActivityDto>>> getMyHistory(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(userActivityService.getMyHistory(authentication.getName())));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserConnectionDto>>> searchUsers(
            @RequestParam(defaultValue = "") String q,
            Authentication authentication) {

        return ResponseEntity.ok(ApiResponse.ok(userSocialService.searchUsers(q, authentication.getName())));
    }

    @GetMapping("/mutuals")
    public ResponseEntity<ApiResponse<List<UserConnectionDto>>> getMutuals(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(userSocialService.getMutuals(authentication.getName())));
    }

    @PostMapping("/{id}/follow")
    public ResponseEntity<ApiResponse<String>> followUser(@PathVariable Long id, Authentication authentication) {
        userSocialService.followUser(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok("Followed"));
    }

    @DeleteMapping("/{id}/follow")
    public ResponseEntity<ApiResponse<String>> unfollowUser(@PathVariable Long id, Authentication authentication) {
        userSocialService.unfollowUser(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok("Unfollowed"));
    }
}