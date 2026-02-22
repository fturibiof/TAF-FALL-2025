package ca.etsmtl.taf.repository;

import ca.etsmtl.taf.entity.Test;
// import org.springframework.data.jpa.repository.JpaRepository; // Not used
// import org.springframework.stereotype.Repository; // Not used
import org.springframework.data.repository.CrudRepository;
import org.springframework.context.annotation.Profile;

@Profile("sql")
public interface TestRepository extends CrudRepository<Test, Integer> {
    
}
