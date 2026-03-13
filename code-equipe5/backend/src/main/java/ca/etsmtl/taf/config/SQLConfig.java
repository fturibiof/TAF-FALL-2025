// SQLConfig.java
package ca.etsmtl.taf.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile; 
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
@Profile("sql")     
@Configuration
@EnableJpaRepositories(
        basePackages = "ca.etsmtl.taf.repository"   // <-- adapte !
)
public class SQLConfig { }
