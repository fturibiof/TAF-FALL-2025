package ca.etsmtl.taf.repository;

import ca.etsmtl.taf.entity.ApiBddJmeterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.stereotype.Repository; // Optional

import org.springframework.context.annotation.Profile;
// import org.springframework.stereotype.Service; // To be removed, not a service


@Profile("sql")
// @Service // to be removed
// @Repository // Optional
public interface ApiBddJmeterRepository extends JpaRepository<ApiBddJmeterEntity, Long> {
}