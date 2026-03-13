package ca.etsmtl.taf.repository;

import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.stereotype.Repository; // Optional

import ca.etsmtl.taf.entity.Project;

@Profile("sql")
// @Repository // Optional 
public interface ProjectRepository extends JpaRepository<Project, Long> {
  Optional<Project> findByName(String name);
}