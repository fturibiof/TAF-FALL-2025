// src/main/java/ca/etsmtl/taf/config/JpaAuditingConfig.java
package ca.etsmtl.taf.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
@Profile("!mongo") // only enable when NOT running mongo profile
public class JpaAuditingConfig { }