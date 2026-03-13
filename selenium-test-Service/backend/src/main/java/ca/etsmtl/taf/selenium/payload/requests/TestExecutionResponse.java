package ca.etsmtl.taf.selenium.payload.requests;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * Response object containing test results and execution metadata
 */
@Data
public class TestExecutionResponse implements Serializable {
    private List<SeleniumResponse> results;
    private long executionTime; // Total execution time in milliseconds
    private boolean parallelExecution; // Whether tests were run in parallel
    private int totalTests;
    private long successfulTests;
}
