package edu.cit.caones.splitshare.repository;

import edu.cit.caones.splitshare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
        Optional<User> findByEmailIgnoreCase(String email);
        boolean existsByEmailIgnoreCase(String email);

        @Query("""
                        select u from User u
                        where lower(u.email) <> lower(:currentEmail)
                            and (
                                     lower(u.firstname) like lower(concat('%', :keyword, '%'))
                                or lower(u.lastname) like lower(concat('%', :keyword, '%'))
                                or lower(u.email) like lower(concat('%', :keyword, '%'))
                            )
                        order by u.firstname asc, u.lastname asc
                        """)
        List<User> searchUsers(@Param("keyword") String keyword, @Param("currentEmail") String currentEmail);
}
