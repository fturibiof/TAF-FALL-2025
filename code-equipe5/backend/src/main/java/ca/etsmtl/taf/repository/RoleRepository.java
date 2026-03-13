package ca.etsmtl.taf.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.stereotype.Repository; // Optional

import org.springframework.context.annotation.Profile;
// import org.springframework.stereotype.Service; // To be removed


import ca.etsmtl.taf.entity.ERole;
import ca.etsmtl.taf.entity.Role;


@Profile("sql")
// @Service // to be removed
// @Repository // optional
public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByName(ERole name);
}