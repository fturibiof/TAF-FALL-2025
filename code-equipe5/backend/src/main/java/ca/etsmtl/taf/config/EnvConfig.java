package ca.etsmtl.taf.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvConfig {

    @Bean
    public Dotenv dotenv() {
        // Look for .env in the BACKEND working directory
        // Do not fail startup if the file is missing
        return Dotenv.configure()
                // .directory(System.getProperty("user.dir")+"/..") // Removed and replace by the 3 lines below
                .directory("./")     // backend/ (working dir when you run mvn)
                .filename(".env")    // exact filename
                .ignoreIfMissing()   // don't crash if .env isn't there
                .load();
    }
}