package com.hackathonbuddy.repository;

import com.hackathonbuddy.entity.Project;
import com.hackathonbuddy.entity.Team;
import com.hackathonbuddy.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN p.team t LEFT JOIN t.members tm " +
           "WHERE (p.team IN :teams) OR p.user = :user OR (t.leader = :user) OR (tm.user = :user AND (tm.status != 'REJECTED' OR tm.status IS NULL)) " +
           "ORDER BY p.createdAt DESC")
    List<Project> findProjectsForTeamsAndUser(@Param("teams") List<Team> teams, @Param("user") User user);

    List<Project> findAllByOrderByCreatedAtDesc();
}
