package com.hackathonbuddy.repository;

import com.hackathonbuddy.entity.Project;
import com.hackathonbuddy.entity.ProjectTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProjectTaskRepository extends JpaRepository<ProjectTask, Long> {
    List<ProjectTask> findByProjectIdOrderByIdAsc(Long projectId);
    List<ProjectTask> findByProjectInOrderByIdAsc(List<Project> projects);

    @Transactional
    void deleteByProjectId(Long projectId);

    @Transactional
    void deleteByProject(Project project);
}
