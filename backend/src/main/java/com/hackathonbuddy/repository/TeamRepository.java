package com.hackathonbuddy.repository;

import com.hackathonbuddy.entity.Team;
import com.hackathonbuddy.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.query.Param;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findByLeader(User leader);

    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN t.members m WHERE (t.leader = :user OR (m.user = :user AND (m.status != 'REJECTED' OR m.status IS NULL))) AND (t.isActive = true OR t.isActive IS NULL) ORDER BY CASE WHEN t.leader = :user THEN 0 ELSE 1 END, t.id DESC")
    List<Team> findTeamsByMember(@Param("user") User user);

    default Optional<Team> findFirstTeamForUser(User user) {
        List<Team> teams = findTeamsByMember(user);
        return teams.isEmpty() ? Optional.empty() : Optional.of(teams.get(0));
    }
}
