package ca.etsmtl.taf.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.stereotype.Repository; // optional for Spring Data JPA
import org.springframework.context.annotation.Profile;
// import org.springframework.stereotype.Service; // Should be removed, not used
// import org.springframework.web.bind.annotation.RestController; // Should be removed, not used  


import ca.etsmtl.taf.entity.User;

@Profile("sql")
// @Service // Should be removed
// @Repository // Optional
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);

  Boolean existsByUsername(String username);

  Boolean existsByEmail(String email);
}
