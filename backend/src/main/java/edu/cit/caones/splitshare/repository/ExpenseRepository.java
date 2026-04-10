package edu.cit.caones.splitshare.repository;

import edu.cit.caones.splitshare.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByGroup_IdOrderByCreatedAtDesc(Long groupId);

    @Query("""
            select distinct e
            from Expense e
            join e.group g
            join g.members gm
            join gm.user u
            where lower(u.email) = lower(:email)
            order by e.createdAt desc
            """)
    List<Expense> findUserActivity(@Param("email") String email);
}