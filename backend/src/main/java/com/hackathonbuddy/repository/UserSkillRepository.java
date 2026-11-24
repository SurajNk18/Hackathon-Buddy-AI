package com.hackathonbuddy.repository;

import com.hackathonbuddy.entity.Skill;
import com.hackathonbuddy.entity.UserSkill;
import com.hackathonbuddy.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {
    List<UserSkill> findByUser(User user);
    Optional<UserSkill> findByUserAndSkill(User user, Skill skill);
    void deleteByUserAndSkillId(User user, Long skillId);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT us.user FROM UserSkill us WHERE LOWER(TRIM(us.skill.name)) = LOWER(TRIM(:skillName)) AND us.user.isActive = true")
    List<User> findUsersBySkillName(@org.springframework.data.repository.query.Param("skillName") String skillName);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT us.user FROM UserSkill us WHERE LOWER(TRIM(us.skill.name)) IN :skillNames AND us.user.isActive = true")
    List<User> findUsersBySkillNames(@org.springframework.data.repository.query.Param("skillNames") java.util.Collection<String> skillNames);

    @org.springframework.data.jpa.repository.Query("SELECT us FROM UserSkill us JOIN FETCH us.skill JOIN FETCH us.user")
    List<UserSkill> findAllWithSkills();
}
