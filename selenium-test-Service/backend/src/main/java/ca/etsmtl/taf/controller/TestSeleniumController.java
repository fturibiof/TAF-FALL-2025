package ca.etsmtl.taf.controller;

import ca.etsmtl.taf.entity.SeleniumTestCase;
import ca.etsmtl.taf.entity.SeleniumActionRequest;
import ca.etsmtl.taf.jmeter.model.HttpTestPlan;
import ca.etsmtl.taf.apiCommunication.SeleniumServiceRequester;
import ca.etsmtl.taf.selenium.payload.SeleniumTestService;
import ca.etsmtl.taf.selenium.payload.requests.SeleniumCase;
import ca.etsmtl.taf.selenium.payload.requests.SeleniumResponse;
import ca.etsmtl.taf.entity.SeleniumCaseResponse;
import ca.etsmtl.taf.dto.SeleniumCaseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/selenium")
public class TestSeleniumController {
     private static final Logger logger = LoggerFactory.getLogger(TestSeleniumController.class);
    private final SeleniumServiceRequester seleniumServiceRequester;
    private final SeleniumTestService seleniumTestService;

    public TestSeleniumController(SeleniumServiceRequester seleniumServiceRequester, SeleniumTestService seleniumTestService) {
        this.seleniumServiceRequester = seleniumServiceRequester;
        this.seleniumTestService = seleniumTestService;
    }

    @PostMapping("/run")
    public ResponseEntity<List<SeleniumResponse>> runTests(@RequestBody List<SeleniumCase> seleniumCases) {
        logger.info("Received {} Selenium cases", seleniumCases.size());
        
        // Convertir SeleniumCase en SeleniumCaseDto et appeler le microservice Selenium
        List<SeleniumResponse> responses = new ArrayList<>();
        
        for (SeleniumCase seleniumCase : seleniumCases) {
            try {
                // Créer le DTO pour le microservice
                SeleniumCaseDto dto = new SeleniumCaseDto();
                dto.setCase_id(seleniumCase.getCase_id());
                dto.setCaseName(seleniumCase.getCaseName());
                
                // Convertir SeleniumAction en SeleniumActionRequest
                List<SeleniumActionRequest> actionRequests = seleniumCase.getActions().stream()
                    .map(action -> {
                        SeleniumActionRequest actionRequest = new SeleniumActionRequest();
                        actionRequest.setAction_id(action.getAction_id());
                        actionRequest.setAction_type_id(action.getAction_type_id());
                        actionRequest.setAction_type_name(action.getAction_type_name());
                        actionRequest.setObject(action.getObject());
                        actionRequest.setInput(action.getInput());
                        actionRequest.setTarget(action.getTarget());
                        return actionRequest;
                    })
                    .collect(Collectors.toList());
                dto.setActions(actionRequests);
                
                // Appeler le microservice Selenium
                logger.info("Calling Selenium microservice for case: {}", seleniumCase.getCaseName());
                SeleniumCaseResponse microserviceResponse = seleniumServiceRequester.sendTestCase(dto).block();
                
                // Convertir la réponse du microservice en SeleniumResponse
                SeleniumResponse response = new SeleniumResponse();
                response.setCase_id(microserviceResponse.getCase_id());
                response.setCaseName(microserviceResponse.getCaseName());
                response.setSuccess(microserviceResponse.isSuccess());
                response.setTimestamp(microserviceResponse.getTimestamp());
                response.setDuration(microserviceResponse.getDuration());
                response.setOutput(microserviceResponse.getOutput());
                response.setSeleniumActions(seleniumCase.getActions());
                
                responses.add(response);
                
                logger.info("Selenium test completed: case={}, success={}, duration={}ms", 
                    seleniumCase.getCaseName(), response.isSuccess(), response.getDuration());
                    
            } catch (Exception e) {
                logger.error("Error calling Selenium microservice for case: {}", seleniumCase.getCaseName(), e);
                
                // Créer une réponse d'erreur
                SeleniumResponse errorResponse = new SeleniumResponse();
                errorResponse.setCase_id(seleniumCase.getCase_id());
                errorResponse.setCaseName(seleniumCase.getCaseName());
                errorResponse.setSuccess(false);
                errorResponse.setOutput("Error calling Selenium microservice: " + e.getMessage());
                errorResponse.setSeleniumActions(seleniumCase.getActions());
                
                responses.add(errorResponse);
            }
        }
        
        logger.info("Finished executing all Selenium cases. total={}, successful={}", 
            responses.size(), responses.stream().filter(SeleniumResponse::isSuccess).count());
        
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/requests")
    public ResponseEntity<List<SeleniumTestCase>> getAllRequests() {
        return ResponseEntity.ok(seleniumTestService.getAllRequests());
    }
}
