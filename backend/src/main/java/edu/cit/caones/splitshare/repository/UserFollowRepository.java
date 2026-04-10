package edu.cit.caones.splitshare.repository;

import edu.cit.caones.splitshare.entity.UserFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {

    boolean existsByFollower_IdAndFollowing_Id(Long followerId, Long followingId);

    Optional<UserFollow> findByFollower_IdAndFollowing_Id(Long followerId, Long followingId);

    @Query("select uf.following.id from UserFollow uf where uf.follower.id = :userId")
    List<Long> findFollowingIds(@Param("userId") Long userId);

    @Query("select uf.follower.id from UserFollow uf where uf.following.id = :userId")
    List<Long> findFollowerIds(@Param("userId") Long userId);
}