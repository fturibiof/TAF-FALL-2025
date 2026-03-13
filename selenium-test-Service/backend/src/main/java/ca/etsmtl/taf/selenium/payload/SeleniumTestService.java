package ca.etsmtl.taf.selenium.payload;

import ca.etsmtl.taf.entity.GatlingRequest;
import ca.etsmtl.taf.entity.SeleniumActionRequest;
import ca.etsmtl.taf.entity.SeleniumTestCase;
import ca.etsmtl.taf.selenium.payload.requests.SeleniumAction;
import ca.etsmtl.taf.selenium.payload.requests.SeleniumCase;
import ca.etsmtl.taf.selenium.payload.requests.SeleniumResponse;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import org.springframework.stereotype.Component;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ca.etsmtl.taf.repository.SeleniumCaseRepository;
import org.openqa.selenium.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URL;
import java.net.URI;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

@Component
public class SeleniumTestService {

    @Autowired
    private SeleniumCaseRepository seleniumCaseRepository;
    private static final Logger logger = LoggerFactory.getLogger(SeleniumTestService.class);

    @SuppressWarnings("deprecation")
    public SeleniumResponse executeTestCase(SeleniumCase seleniumCase) {
        logger.info("im Executing Selenium case");
        List<SeleniumAction> seleniumActions = seleniumCase.getActions();
        // List<SeleniumAction> seleniumActionRequests = seleniumActions;
        // Convertir les SeleniumActions en SeleniumActionRequest
        List<SeleniumActionRequest> seleniumActionRequests = convertToSeleniumActionRequests(seleniumActions);

        SeleniumResponse seleniumResponse = new SeleniumResponse();
        seleniumResponse.setCase_id(seleniumCase.getCase_id());
        seleniumResponse.setCaseName(seleniumCase.getCaseName());
        seleniumResponse.setSeleniumActions(seleniumActions);
        long currentTimestamp = (new Timestamp(System.currentTimeMillis())).getTime();
        seleniumResponse.setTimestamp(currentTimestamp / 1000);
        ChromeOptions options = new ChromeOptions();
        // WebDriver driver = new ChromeDriver();
        // Must run headless in Docker/WSL2
        options.addArguments("--headless=new"); // "--headless-new" is recommended for Chrome 109+
        options.addArguments("--no-sandbox"); // Required in containers
        options.addArguments("--disable-dev-shm-usage"); // Avoid /dev/shm issues
        options.addArguments("--disable-gpu"); // Optional
        options.addArguments("--remote-allow-origins=*"); // Needed for Selenium 4.19+
        
        // Additional optimizations for parallel execution
        options.addArguments("--disable-software-rasterizer"); // Reduce CPU usage
        options.addArguments("--disable-extensions"); // Faster startup
        options.addArguments("--disable-background-networking"); // Reduce network overhead
        options.addArguments("--disable-sync"); // Disable sync services
        options.addArguments("--disable-translate"); // Disable translate service
        options.addArguments("--disable-default-apps"); // Don't load default apps
        options.addArguments("--disable-background-timer-throttling"); // Better for test timing
        options.addArguments("--disable-renderer-backgrounding"); // Keep renderer active
        options.addArguments("--disable-backgrounding-occluded-windows"); // Maintain performance
        options.addArguments("--disable-ipc-flooding-protection"); // Better for rapid actions
        options.addArguments("--disable-hang-monitor"); // Prevent unnecessary checks
        options.addArguments("--disable-popup-blocking"); // Allow popups if needed
        options.addArguments("--disable-prompt-on-repost"); // Auto-confirm reposts
        options.addArguments("--disable-domain-reliability"); // Reduce background requests
        options.addArguments("--disable-component-extensions-with-background-pages"); // Less memory
        options.addArguments("--window-size=1280,720"); // Smaller viewport = less memory
        options.addArguments("--blink-settings=imagesEnabled=false"); // Disable images for faster loading
        
        WebDriver driver = new ChromeDriver(options);

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        long startTime = System.currentTimeMillis();

        // Créer l'objet SeleniumTestCase avec les informations nécessaires
        SeleniumTestCase seleniumTestCase = new SeleniumTestCase();
        seleniumTestCase.setCase_id(seleniumCase.getCase_id());
        seleniumTestCase.setCaseName(seleniumCase.getCaseName());
        seleniumTestCase.setActions(seleniumCase.getActions());
        seleniumTestCase.setTimestamp(currentTimestamp / 1000);

        // Liste pour stocker les résultats des actions
        List<String> actionResults = new ArrayList<>();

        try {
            logger.info("2.ste p 2Executing Selenium case: {}", seleniumCase.getCase_id());
            logger.info("SeleniumActionRequest: {}", seleniumActionRequests);
            logger.info("Finished executing actions for case: {}", actionResults);
            seleniumResponse.setSuccess(true);
            for (SeleniumActionRequest seleniumActionRequest : seleniumActionRequests) {
                String actionResult = "";

                try {
                    logger.info("changed Executing action type: {}", seleniumActionRequest.getAction_type_id());
                    /*
                     * if (seleniumActionRequest.getAction_type_id() == 1) {
                     * driver.get(seleniumActionRequest.getInput());
                     * actionResult = "Success";
                     * }
                     */
                    switch (seleniumActionRequest.getAction_type_id()) {
                        case 1: // goToUrl
                            logger.info("!go to : {}", seleniumActionRequest.getInput());
                            String inputUrl = seleniumActionRequest.getInput();
                            // --- URL VALIDATION BEFORE SELENIUM ---//
                            try {
                                new URL(inputUrl).toURI();
                                // Validates both URL format + URI syntax
                                //
                            } catch (Exception e) {
                                String outputMessage = "Invalid URL format: " + inputUrl;
                                logger.info("outputMessage: {}", outputMessage);
                                seleniumResponse.setSuccess(false);
                                actionResult += "<br>Failure when executing test case goToUrl: " + outputMessage;
                                break;
                                // Stop here — do NOT call driver.get()
                            }
                            driver.get(seleniumActionRequest.getInput());
                            logger.info("!Navigated to URL successfully.");
                            
                            if (!driver.getCurrentUrl().equals(seleniumActionRequest.getInput())) {
                                String outputMessage = "Failed to navigate to URL: " + seleniumActionRequest.getInput();
                                logger.info("outputMessage: {}", outputMessage);
                                seleniumResponse.setSuccess(false);
                                actionResult += "<br>Failure when executing test case goToUrl: " + outputMessage;
                                // return finalizeTest(driver, seleniumResponse, startTime, false,
                                // outputMessage);
                            }
                            // Utilisez Duration.ofSeconds si vous êtes en Selenium 4+
                            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
                            break;
                        case 2: // FillField
                            logger.info("fill : {} with {}", seleniumActionRequest.getObject(),
                                    seleniumActionRequest.getInput());
                            WebElement textBox = driver.findElement(By.name(seleniumActionRequest.getObject()));
                            logger.info("textBox found: {}", textBox != null);
                            textBox.sendKeys(seleniumActionRequest.getInput());
                            if (!textBox.getAttribute("value").equals(seleniumActionRequest.getInput())) {
                                String outputMessage = "Failed to fill the field " + seleniumActionRequest.getObject()
                                        + " with " + seleniumActionRequest.getInput();
                                logger.info("outputMessage: {}", outputMessage);
                                seleniumResponse.setSuccess(false);
                                actionResult += "<br>Failure when executing test case FillField: " + outputMessage;
                                // return finalizeTest(driver, seleniumResponse, startTime, false,
                                // outputMessage);
                            }
                            break;
                        case 4: // GetPageTitle
                            logger.info("Verifying page title...");
                            String pageTitle = driver.getTitle();

                            if (!pageTitle.equals(seleniumActionRequest.getTarget())) {
                                String outputMessage = "Page title is \""
                                        + pageTitle + "\" instead of \""
                                        + seleniumActionRequest.getTarget() + "\"";
                                logger.info("outputMessage: {}", outputMessage);
                                seleniumResponse.setSuccess(false);

                                actionResult += "<br>Failure when executing test case  GetPageTitle " + outputMessage;
                                // return finalizeTest(driver, seleniumResponse, startTime, false,
                                // outputMessage);
                            }
                            break;
                        case 3: // GetAttribute
                            WebElement webElement = driver.findElement(By.name(seleniumActionRequest.getTarget()));
                            String pageAttribute = webElement.getAttribute(seleniumActionRequest.getObject());
                            logger.info("pageAttribute: {}", pageAttribute);
                            logger.info("Expected attribute value: {}", seleniumActionRequest.getInput());

                            if (!pageAttribute.equals(seleniumActionRequest.getInput())) {
                                String outputMessage = "Attribute " + seleniumActionRequest.getObject()
                                        + " of " + seleniumActionRequest.getTarget()
                                        + " is " + pageAttribute
                                        + " instead of " + seleniumActionRequest.getInput();
                                seleniumResponse.setSuccess(false);
                                logger.info("outputMessage: {}", outputMessage);
                                actionResult += "<br>Failure when executing test case GetAttribute: " + outputMessage;
                            }
                            break;
                    }
                } catch (Exception e) {
                    // actionResult = "Failure: " + e.getMessage();
                }

                // Si actionResult est null, le remplacer par "Failed"
                if (actionResult == null) {
                    // actionResult = "Failed";
                }

                actionResults.add(actionResult);
            }

            logger.info("SeleniumActionRequest: {}", seleniumActionRequests);
            logger.info("Finished executing actions for case: {}", actionResults);
            driver.quit();
            seleniumResponse.setDuration(System.currentTimeMillis() - startTime);
            seleniumResponse.setOutput(String.join("\n", actionResults));
            // seleniumResponse.setSuccess(true);

            // Ajouter les résultats des actions à l'objet SeleniumTestCase
            seleniumTestCase.setActionResults(actionResults);

            // Sauvegarde des résultats dans MongoDB après exécution
            seleniumCaseRepository.save(seleniumTestCase);

        } catch (Exception e) {
            logger.error("Error executing Selenium case: {}", e.getMessage());
            setFailureResponse(seleniumResponse, driver, startTime, e.getMessage());
            seleniumTestCase.setErrorMessage(e.getMessage());
            seleniumCaseRepository.save(seleniumTestCase);
        }

        return seleniumResponse;
    }

    private List<SeleniumActionRequest> convertToSeleniumActionRequests(List<SeleniumAction> seleniumActions) {
        List<SeleniumActionRequest> seleniumActionRequests = new ArrayList<>();

        for (SeleniumAction seleniumAction : seleniumActions) {
            SeleniumActionRequest actionRequest = new SeleniumActionRequest();
            actionRequest.setAction_type_id(seleniumAction.getAction_type_id());
            actionRequest.setInput(seleniumAction.getInput());
            actionRequest.setObject(seleniumAction.getObject());
            actionRequest.setTarget(seleniumAction.getTarget());
            seleniumActionRequests.add(actionRequest);
        }

        return seleniumActionRequests;
    }

    private void setFailureResponse(SeleniumResponse seleniumResponse, WebDriver driver, long startTime,
            String message) {
        driver.quit();
        seleniumResponse.setSuccess(false);
        seleniumResponse.setOutput(message);
        seleniumResponse.setDuration(System.currentTimeMillis() - startTime);
    }

    public List<SeleniumTestCase> getAllRequests() {
        return seleniumCaseRepository.findAll();
    }

}
