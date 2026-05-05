package edu.cit.caones.splitshare.features.users.controller;

import edu.cit.caones.splitshare.shared.dto.request.UpdateProfileRequest;
import edu.cit.caones.splitshare.shared.dto.response.ApiResponse;
import edu.cit.caones.splitshare.shared.dto.response.UserActivityDto;
import edu.cit.caones.splitshare.shared.dto.response.UserConnectionDto;
import edu.cit.caones.splitshare.shared.dto.response.UserDto;
import edu.cit.caones.splitshare.shared.dto.response.UserProfileStatsDto;
import edu.cit.caones.splitshare.features.users.service.UserActivityService;
import edu.cit.caones.splitshare.features.users.service.UserSocialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/me/stats")
    public ResponseEntity<ApiResponse<UserProfileStatsDto>> getProfileStats(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(userSocialService.getProfileStats(authentication.getName())));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(@RequestBody UpdateProfileRequest request, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(userSocialService.updateProfile(request, authentication.getName())));
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

    @GetMapping("/me/followers")
    public ResponseEntity<ApiResponse<List<UserConnectionDto>>> getFollowers(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(userSocialService.getFollowers(authentication.getName())));
    }

    @GetMapping("/me/following")
    public ResponseEntity<ApiResponse<List<UserConnectionDto>>> getFollowing(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(userSocialService.getFollowing(authentication.getName())));
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
