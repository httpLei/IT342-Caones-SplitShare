package edu.cit.caones.splitshare.repository;

import edu.cit.caones.splitshare.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {

    @Query("select distinct g from Group g join g.members m join m.user u where u.email = :email order by g.createdAt desc")
    List<Group> findAllVisibleToUser(@Param("email") String email);
}