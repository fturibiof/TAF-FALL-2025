package ca.etsmtl.selenium.tests;

import ca.etsmtl.selenium.requests.payload.request.SeleniumAction;
import ca.etsmtl.selenium.requests.payload.request.SeleniumCase;
import ca.etsmtl.selenium.requests.payload.request.SeleniumResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exemple de tests Selenium parallèles avec JUnit 5
 * 
 * Pour activer la parallélisation, ajouter dans junit-platform.properties :
 * junit.jupiter.execution.parallel.enabled = true
 * junit.jupiter.execution.parallel.mode.default = concurrent
 * junit.jupiter.execution.parallel.config.strategy = dynamic
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Execution(ExecutionMode.CONCURRENT)  // Active la parallélisation pour cette classe
public class ParallelSeleniumTests {

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String SELENIUM_ENDPOINT = "/microservice/selenium/test";

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    public void testCase1_GoogleSearch() {
        SeleniumCase testCase = createTestCase("Test 1 - Google", 
            "https://www.google.com", "Google");
        
        ResponseEntity<SeleniumResponse> response = restTemplate
            .postForEntity(SELENIUM_ENDPOINT, testCase, SeleniumResponse.class);
        
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        System.out.println("✅ Test 1 completed");
    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    public void testCase2_BingSearch() {
        SeleniumCase testCase = createTestCase("Test 2 - Bing", 
            "https://www.bing.com", "Bing");
        
        ResponseEntity<SeleniumResponse> response = restTemplate
            .postForEntity(SELENIUM_ENDPOINT, testCase, SeleniumResponse.class);
        
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        System.out.println("✅ Test 2 completed");
    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    public void testCase3_LocalUI() {
        // Ce test utilise l'UI locale - une seule instance suffit pour les tests parallèles
        // car chaque test lance sa propre session de navigateur
        SeleniumCase testCase = createTestCase("Test 3 - Local UI", 
            "http://localhost:4200", "My App");
        
        ResponseEntity<SeleniumResponse> response = restTemplate
            .postForEntity(SELENIUM_ENDPOINT, testCase, SeleniumResponse.class);
        
        assertNotNull(response.getBody());
        System.out.println("✅ Test 3 completed");
    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    public void testCase4_AnotherTest() {
        SeleniumCase testCase = createTestCase("Test 4 - Another", 
            "https://www.example.com", "Example Domain");
        
        ResponseEntity<SeleniumResponse> response = restTemplate
            .postForEntity(SELENIUM_ENDPOINT, testCase, SeleniumResponse.class);
        
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        System.out.println("✅ Test 4 completed");
    }

    // Helper method pour créer un test case simple
    private SeleniumCase createTestCase(String caseName, String url, String expectedTitle) {
        SeleniumCase testCase = new SeleniumCase();
        testCase.setCase_id((int) (System.currentTimeMillis() % Integer.MAX_VALUE));
        testCase.setCaseName(caseName);
        
        List<SeleniumAction> actions = new ArrayList<>();
        
        // Action 1: Naviguer vers l'URL
        SeleniumAction goToUrl = new SeleniumAction();
        goToUrl.setAction_type_id(1);  // goToUrl
        goToUrl.setAction_type_name("goToUrl");
        goToUrl.setInput(url);
        actions.add(goToUrl);
        
        // Action 2: Vérifier le titre de la page
        SeleniumAction checkTitle = new SeleniumAction();
        checkTitle.setAction_type_id(4);  // GetPageTitle
        checkTitle.setAction_type_name("GetPageTitle");
        checkTitle.setTarget(expectedTitle);
        actions.add(checkTitle);
        
        testCase.setActions(actions);
        return testCase;
    }
}
