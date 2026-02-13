package ca.etsmtl.taf.controller;

import ca.etsmtl.taf.entity.SeleniumTestCase;
import ca.etsmtl.taf.entity.SeleniumActionRequest;
import ca.etsmtl.taf.jmeter.model.HttpTestPlan;
import ca.etsmtl.taf.apiCommunication.SeleniumServiceRequester;
import ca.etsmtl.taf.selenium.payload.SeleniumTestService;
import ca.etsmtl.taf.selenium.payload.requests.SeleniumCase;
import ca.etsmtl.taf.selenium.payload.requests.SeleniumResponse;
import ca.etsmtl.taf.selenium.payload.requests.TestExecutionRequest;
import ca.etsmtl.taf.selenium.payload.requests.TestExecutionResponse;
import ca.etsmtl.taf.entity.SeleniumCaseResponse;
import ca.etsmtl.taf.dto.SeleniumCaseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.Collections;
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
    public ResponseEntity<TestExecutionResponse> runTests(@RequestBody TestExecutionRequest request) {
        List<SeleniumCase> seleniumCases = request.getCases();
        boolean parallelExecution = request.isParallelExecution();
        
        logger.info("Received {} Selenium cases, parallel execution: {}", seleniumCases.size(), parallelExecution);
        
        long startTime = System.currentTimeMillis();
        List<SeleniumResponse> responses;
        
        if (parallelExecution) {
            // Execute tests in parallel
            responses = executeTestsInParallel(seleniumCases);
        } else {
            // Execute tests sequentially
            responses = executeTestsSequentially(seleniumCases);
        }
        
        long executionTime = System.currentTimeMillis() - startTime;
        
        logger.info("Finished executing all Selenium cases. total={}, successful={}, execution time={}ms", 
            responses.size(), responses.stream().filter(SeleniumResponse::isSuccess).count(), executionTime);
        
        // Create response with execution time
        TestExecutionResponse executionResponse = new TestExecutionResponse();
        executionResponse.setResults(responses);
        executionResponse.setExecutionTime(executionTime);
        executionResponse.setParallelExecution(parallelExecution);
        executionResponse.setTotalTests(responses.size());
        executionResponse.setSuccessfulTests(responses.stream().filter(SeleniumResponse::isSuccess).count());
        
        return ResponseEntity.ok(executionResponse);
    }

    @GetMapping("/requests")
    public ResponseEntity<List<SeleniumTestCase>> getAllRequests() {
        return ResponseEntity.ok(seleniumTestService.getAllRequests());
    }
    
    /**
     * Execute tests sequentially (one after another)
     */
    private List<SeleniumResponse> executeTestsSequentially(List<SeleniumCase> seleniumCases) {
        List<SeleniumResponse> responses = new ArrayList<>();
        
        for (SeleniumCase seleniumCase : seleniumCases) {
            responses.add(executeSingleTest(seleniumCase));
        }
        
        return responses;
    }
    
    /**
     * Execute tests in parallel using ExecutorService
     */
    private List<SeleniumResponse> executeTestsInParallel(List<SeleniumCase> seleniumCases) {
        // Create a thread pool optimized for I/O-bound operations (Selenium tests)
        // Selenium tests are I/O-bound (waiting for browser), not CPU-bound
        // Use 3x CPU cores or number of tests, whichever is smaller, but at least 4
        int cpuCores = Runtime.getRuntime().availableProcessors();
        int optimalThreads = Math.max(4, Math.min(cpuCores * 3, 20)); // Cap at 20 to avoid resource exhaustion
        int threadPoolSize = Math.min(seleniumCases.size(), optimalThreads);
        
        logger.info("Parallel execution with {} threads (CPU cores: {}, test cases: {})", 
                    threadPoolSize, cpuCores, seleniumCases.size());
        
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
        List<SeleniumResponse> responses = Collections.synchronizedList(new ArrayList<>());
        
        try {
            // Submit all test cases as separate tasks
            List<Future<?>> futures = new ArrayList<>();
            
            int taskIndex = 0;
            for (SeleniumCase seleniumCase : seleniumCases) {
                final int currentIndex = taskIndex;
                
                Future<?> future = executorService.submit(() -> {
                    // Stagger driver initialization to reduce resource contention
                    // Each thread waits a small amount before starting
                    try {
                        Thread.sleep(currentIndex * 200L); // 200ms delay between each driver startup
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.warn("Stagger delay interrupted for test case {}", currentIndex);
                    }
                    
                    SeleniumResponse response = executeSingleTest(seleniumCase);
                    responses.add(response);
                });
                futures.add(future);
                taskIndex++;
            }
            
            // Wait for all tasks to complete
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    logger.error("Error executing test in parallel", e);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("Thread interrupted while waiting for test execution", e);
                }
            }
        } finally {
            executorService.shutdown();
        }
        
        return responses;
    }
    
    /**
     * Execute a single test case
     */
    private SeleniumResponse executeSingleTest(SeleniumCase seleniumCase) {
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
            
            logger.info("Selenium test completed: case={}, success={}, duration={}ms", 
                seleniumCase.getCaseName(), response.isSuccess(), response.getDuration());
            
            return response;
                
        } catch (Exception e) {
            logger.error("Error calling Selenium microservice for case: {}", seleniumCase.getCaseName(), e);
            
            // Créer une réponse d'erreur
            SeleniumResponse errorResponse = new SeleniumResponse();
            errorResponse.setCase_id(seleniumCase.getCase_id());
            errorResponse.setCaseName(seleniumCase.getCaseName());
            errorResponse.setSuccess(false);
            errorResponse.setOutput("Error calling Selenium microservice: " + e.getMessage());
            errorResponse.setSeleniumActions(seleniumCase.getActions());
            
            return errorResponse;
        }
    }
}
