package ca.etsmtl.taf.selenium.payload.requests;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * Request object for test execution with parallelism control
 */
@Data
public class TestExecutionRequest implements Serializable {
    private List<SeleniumCase> cases;
    private boolean parallelExecution = true; // Default to parallel execution for better performance
}
