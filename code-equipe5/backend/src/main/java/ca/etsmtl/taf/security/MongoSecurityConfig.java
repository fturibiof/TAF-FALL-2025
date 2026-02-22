// ============================================================================
// MongoSecurityConfig.java
// ----------------------------------------------------------------------------
// Purpose:
//   Provide a minimal, profile-specific Spring Security configuration that
//   disables authentication/authorization when running the backend in
//   Mongo-only mode (profile "mongo").
//
// Notes:
//   - This is intentionally permissive (permitAll) ONLY for dev/local runs.
//   - Do NOT use this in production.
//   - If you need to protect some endpoints in mongo mode, replace
//     `.anyRequest().permitAll()` with matchers that fit your needs.
// ============================================================================

package ca.etsmtl.taf.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// In Spring Boot 2.6.x, WebSecurityConfigurerAdapter is still supported.
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Profile("mongo")          // <-- Only active when you run with profile "mongo"
@Configuration
@EnableWebSecurity
public class MongoSecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * Configure HTTP security for the "mongo" profile.
     * This configuration:
     *  - Disables CSRF (typical for stateless APIs & local development)
     *  - Permits all requests to any endpoint (no login required)
     *  - Disables frameOptions (harmless for APIs; helpful if you embed consoles)
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // For local development of APIs, CSRF can be disabled.
            .csrf().disable()

            // Authorization rules: in mongo profile, we allow everything by default.
            .authorizeRequests()
                .anyRequest().permitAll()   // <-- No authentication required on any route
            .and()

            // Optional hardening tweak for dev consoles; harmless for normal APIs.
            .headers().frameOptions().disable();
    }
}