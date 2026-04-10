package edu.cit.caones.splitshare.service;

import edu.cit.caones.splitshare.dto.response.UserConnectionDto;
import edu.cit.caones.splitshare.entity.User;
import edu.cit.caones.splitshare.entity.UserFollow;
import edu.cit.caones.splitshare.repository.UserFollowRepository;
import edu.cit.caones.splitshare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserSocialService {

    private final UserRepository userRepository;
    private final UserFollowRepository userFollowRepository;

    @Transactional(readOnly = true)
    public List<UserConnectionDto> searchUsers(String query, String currentUserEmail) {
        User me = getCurrentUser(currentUserEmail);
        String keyword = query == null ? "" : query.trim();
        if (keyword.isBlank()) {
            return List.of();
        }

        Set<Long> followingIds = new HashSet<>(userFollowRepository.findFollowingIds(me.getId()));
        Set<Long> followerIds = new HashSet<>(userFollowRepository.findFollowerIds(me.getId()));

        return userRepository.searchUsers(keyword, me.getEmail())
                .stream()
                .limit(20)
                .map(user -> toConnectionDto(user, followingIds, followerIds))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserConnectionDto> getMutuals(String currentUserEmail) {
        User me = getCurrentUser(currentUserEmail);

        Set<Long> followingIds = new HashSet<>(userFollowRepository.findFollowingIds(me.getId()));
        Set<Long> followerIds = new HashSet<>(userFollowRepository.findFollowerIds(me.getId()));
        followingIds.retainAll(followerIds);

        if (followingIds.isEmpty()) {
            return List.of();
        }

        return userRepository.findAllById(followingIds)
                .stream()
                .map(user -> UserConnectionDto.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .firstname(user.getFirstname())
                        .lastname(user.getLastname())
                        .following(true)
                        .followedBy(true)
                        .mutual(true)
                        .build())
                .sorted((a, b) -> (a.getFirstname() + " " + a.getLastname()).compareToIgnoreCase(b.getFirstname() + " " + b.getLastname()))
                .toList();
    }

    @Transactional
    public void followUser(Long targetUserId, String currentUserEmail) {
        User me = getCurrentUser(currentUserEmail);
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (me.getId().equals(target.getId())) {
            throw new IllegalArgumentException("You cannot follow yourself");
        }

        if (userFollowRepository.existsByFollower_IdAndFollowing_Id(me.getId(), target.getId())) {
            return;
        }

        userFollowRepository.save(UserFollow.builder()
                .follower(me)
                .following(target)
                .build());
    }

    @Transactional
    public void unfollowUser(Long targetUserId, String currentUserEmail) {
        User me = getCurrentUser(currentUserEmail);
        userFollowRepository.findByFollower_IdAndFollowing_Id(me.getId(), targetUserId)
                .ifPresent(userFollowRepository::delete);
    }

    @Transactional(readOnly = true)
    public boolean isMutualByEmails(String emailA, String emailB) {
        User a = userRepository.findByEmailIgnoreCase(emailA)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + emailA));
        User b = userRepository.findByEmailIgnoreCase(emailB)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + emailB));

        return userFollowRepository.existsByFollower_IdAndFollowing_Id(a.getId(), b.getId())
                && userFollowRepository.existsByFollower_IdAndFollowing_Id(b.getId(), a.getId());
    }

    private User getCurrentUser(String currentUserEmail) {
        return userRepository.findByEmailIgnoreCase(currentUserEmail)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + currentUserEmail));
    }

    private UserConnectionDto toConnectionDto(User user, Set<Long> followingIds, Set<Long> followerIds) {
        boolean following = followingIds.contains(user.getId());
        boolean followedBy = followerIds.contains(user.getId());
        boolean mutual = following && followedBy;

        return UserConnectionDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .following(following)
                .followedBy(followedBy)
                .mutual(mutual)
                .build();
    }
}