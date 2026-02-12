package ca.etsmtl.taf.controller;

import ca.etsmtl.taf.entity.SeleniumTestCase;
import ca.etsmtl.taf.jmeter.model.HttpTestPlan;
import ca.etsmtl.taf.selenium.payload.SeleniumTestService;
import ca.etsmtl.taf.selenium.payload.requests.SeleniumCase;
import ca.etsmtl.taf.selenium.payload.requests.SeleniumResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/selenium")
public class TestSeleniumController {
     private static final Logger logger = LoggerFactory.getLogger(TestSeleniumController.class);
    private final SeleniumTestService seleniumTestService;

    public TestSeleniumController(SeleniumTestService seleniumTestService) {
        this.seleniumTestService = seleniumTestService;
    }

    @PostMapping("/run")
    public ResponseEntity<List<SeleniumResponse>> runTests(@RequestBody List<SeleniumCase> seleniumCases) {
        logger.info("Received {} Selenium cases", seleniumCases.size());
        // Exécute chaque cas de test avec SeleniumTestService
        List<SeleniumResponse> responses = seleniumCases.stream()
                .map(seleniumTestService::executeTestCase)
                .collect(Collectors.toList());
        logger.info("Finished executing all Selenium cases. responses={}", responses);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/requests")
    public ResponseEntity<List<SeleniumTestCase>> getAllRequests() {
        return ResponseEntity.ok(seleniumTestService.getAllRequests());
    }
}
