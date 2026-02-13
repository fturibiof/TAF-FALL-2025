package ca.etsmtl.selenium.requests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.TimeoutException; // Ajouté pour gestion des attentes
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;
import java.util.List;
//import java.util.NoSuchElementException;
import org.openqa.selenium.NoSuchElementException;
import java.io.File;
import java.sql.Timestamp;

import org.springframework.web.bind.annotation.*;

import ca.etsmtl.selenium.requests.payload.request.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URL;
import java.net.URI;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.TimeoutException;

import org.openqa.selenium.JavascriptExecutor;

import ca.etsmtl.selenium.config.UiInstanceProvider;
import org.springframework.beans.factory.annotation.Autowired;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/microservice/selenium")
public class UseSelenium {
    private static final Logger logger = LoggerFactory.getLogger(UseSelenium.class);
    
    @Autowired
    private UiInstanceProvider uiInstanceProvider;

    @PostMapping("/test")
    public SeleniumResponse testWithSelenium(@RequestBody SeleniumCase seleniumCase) {
        List<SeleniumAction> seleniumActions = seleniumCase.getActions();

        // Sélectionner une instance UI pour ce test (load balancing round-robin)
        String selectedUiUrl = uiInstanceProvider.getNextUiUrl();
        logger.info("Test case '{}' assigned to UI instance: {}", seleniumCase.getCaseName(), selectedUiUrl);

        SeleniumResponse seleniumResponse = new SeleniumResponse();
        seleniumResponse.setCase_id(seleniumCase.getCase_id());
        seleniumResponse.setCaseName(seleniumCase.getCaseName());
        seleniumResponse.setSeleniumActions(seleniumActions);
        long currentTimestamp = (new Timestamp(System.currentTimeMillis())).getTime();
        seleniumResponse.setTimestamp(currentTimestamp / 1000);

        // Déclaration du driver ici pour qu'il soit accessible au catch externe
        WebDriver driver = null;
        long startTime = System.currentTimeMillis();

        try {
            logger.info("inside useSelenium 1");

            // Vérifier les chemins disponibles
            logger.info("Checking paths...");
            String[] possibleChromePaths = {
                    "/usr/bin/google-chrome",
                    "/usr/bin/google-chrome-stable",
                    "/usr/bin/chromium",
                    "/usr/bin/chromium-browser"
            };

            String chromePath = null;
            for (String path : possibleChromePaths) {
                java.io.File f = new java.io.File(path);
                if (f.exists()) {
                    chromePath = path;
                    logger.info("Found Chrome/Chromium at: {}", path);
                    break;
                }
            }

            if (chromePath == null) {
                logger.error("Chrome/Chromium binary not found!");
                throw new RuntimeException("Chrome/Chromium binary not found in any expected location");
            }

            // Note: Selenium Manager (4.x+) will automatically download and manage
            // ChromeDriver
            // No need to set webdriver.chrome.driver manually

            ChromeOptions options = new ChromeOptions();
            options.setBinary(chromePath);
            options.addArguments("--no-sandbox");
            options.addArguments("--headless=new");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--window-size=1920x1080");
            options.addArguments("--disable-software-rasterizer");

            logger.info("Creating ChromeDriver with binary: {}", chromePath);
            driver = new ChromeDriver(options);
            logger.info("inside useSelenium ChromeDriver initialized successfully.");
            // Le temps d'attente implicite est mieux défini au niveau du driver
            // driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10)); // Exemple
            // de bonne pratique

            // Bloc try pour l'exécution des actions
            try {
                for (SeleniumAction seleniumAction : seleniumActions) {
                    System.out.println("action type name : " + seleniumAction.getAction_type_name());
                    logger.info("inside useSelenium executing action type: {}", seleniumAction.getAction_type_name());
                    switch (seleniumAction.getAction_type_id()) {
                        case 1: // goToUrl
                            System.out.println("go to : " + seleniumAction.getInput());
                            // Validation de l'URL avant de naviguer
                            try {
                                new URL(seleniumAction.getInput()).toURI();

                            } catch (Exception e) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "L'URL " + seleniumAction.getInput() + " est invalide.");
                            }
                            try {
                                // Définir un timeout de chargement de page pour éviter les blocages
                                driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));

                                // Naviguer vers l'URL
                                driver.get(seleniumAction.getInput());

                            } catch (TimeoutException e) {
                                // Gérer les timeout de chargement de page
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "Timeout de chargement de page pour l'URL: " + seleniumAction.getInput());

                            } catch (WebDriverException e) {
                                // Gérer les erreurs de navigation (ex: DNS, serveur injoignable, etc.)
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "Impossible d'accéder à l'URL: " + seleniumAction.getInput());
                            }
                            break;
                        case 2: // FillField
                            System.out.println(
                                    "fill : " + seleniumAction.getObject() + " with " + seleniumAction.getInput());
                            WebElement textBox;

                            try {
                                textBox = driver.findElement(By.name(seleniumAction.getObject()));
                            } catch (NoSuchElementException e) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "Champ de saisie '" + seleniumAction.getObject() + "' introuvable.");
                            }

                            textBox.sendKeys(seleniumAction.getInput());
                            break;
                        case 3: // GetAttribute
                            WebElement webElement;

                            try {
                                webElement = driver.findElement(By.name(seleniumAction.getTarget()));
                            } catch (NoSuchElementException e) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "Élément '" + seleniumAction.getTarget() + "' introuvable.");
                            }

                            String pageAttribute = webElement.getAttribute(seleniumAction.getObject());

                            if (pageAttribute == null) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "Attribut '" + seleniumAction.getObject()
                                                + "' introuvable sur l'élément '"
                                                + seleniumAction.getTarget() + "'.");
                            }

                            if (!pageAttribute.equals(seleniumAction.getInput())) {
                                String outputMessage = "Attribut '" + seleniumAction.getObject()
                                        + "' de '" + seleniumAction.getTarget()
                                        + "' est '" + pageAttribute
                                        + "' au lieu de '" + seleniumAction.getInput() + "'";
                                return finalizeTest(driver, seleniumResponse, startTime, false, outputMessage);
                            }

                            break;
                        case 4: // GetPageTitle
                            System.out.println("Verifying page title...");
                            String pageTitle = driver.getTitle();

                            if (!pageTitle.equals(seleniumAction.getTarget())) {
                                String outputMessage = "Titre de la page est \""
                                        + pageTitle + "\" au lieu de \""
                                        + seleniumAction.getTarget() + "\"";
                                return finalizeTest(driver, seleniumResponse, startTime, false, outputMessage);
                            }
                            break;
                        case 5: // Clear
                            try {
                                WebElement textBoxToClear = driver.findElement(By.name(seleniumAction.getObject()));

                                // Optional: check if interactable
                                if (!textBoxToClear.isDisplayed() || !textBoxToClear.isEnabled()) {
                                    return finalizeTest(driver, seleniumResponse, startTime, false,
                                            "Champ de saisie '" + seleniumAction.getObject()
                                                    + "' non interactif pour effacement.");
                                }

                                textBoxToClear.clear();

                            } catch (org.openqa.selenium.NoSuchElementException e) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "Champ de saisie '" + seleniumAction.getObject()
                                                + "' introuvable pour effacement.");
                            }
                            break;
                        case 6: // Click
                            try {
                                WebElement submitButton = driver.findElement(By.name(seleniumAction.getObject()));

                                try {
                                    submitButton.click();
                                } catch (org.openqa.selenium.ElementNotInteractableException e) {
                                    return finalizeTest(driver, seleniumResponse, startTime, false,
                                            "Bouton '" + seleniumAction.getObject() + "' non interactif pour clic.");
                                }

                            } catch (org.openqa.selenium.NoSuchElementException e) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "Bouton '" + seleniumAction.getObject() + "' introuvable pour clic.");
                            }
                            break;
                        case 7: // isDisplayed
                            try {
                                WebElement message = driver.findElement(By.name(seleniumAction.getObject()));

                                // Optional: check if visible
                                if (!message.isDisplayed()) {
                                    return finalizeTest(driver, seleniumResponse, startTime, false,
                                            "Message '" + seleniumAction.getObject() + "' n'est pas affiché.");
                                }

                                // Access the text if needed
                                String text = message.getText();
                                System.out.println("Message text: " + text);

                            } catch (org.openqa.selenium.NoSuchElementException e) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "Message '" + seleniumAction.getObject() + "' introuvable.");
                            }
                            break;
                        case 8: // VerifyText
                            System.out.println("Verify text of : " + seleniumAction.getObject() + " is "
                                    + seleniumAction.getTarget());

                            WebElement textElement;

                            try {
                                // Step 1: find the element
                                textElement = driver.findElement(By.name(seleniumAction.getObject()));
                            } catch (NoSuchElementException ex) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "L'Element '" + seleniumAction.getObject()
                                                + "' est introuvable pour vérification du texte.");
                            }

                            try {
                                // Step 2: verify text
                                String actualText = textElement.getText().trim();
                                String expectedText = seleniumAction.getTarget().trim();

                                if (!actualText.equals(expectedText)) {
                                    return finalizeTest(driver, seleniumResponse, startTime, false,
                                            "Le texte de '" + seleniumAction.getObject() + "' est '" + actualText
                                                    + "' au lieu de '" + expectedText + "'");
                                }

                            } catch (Exception ex) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "Erreur lors de la vérification du texte de l'élément '"
                                                + seleniumAction.getObject()
                                                + "' - " + ex.getMessage());
                            }

                            break;

                        case 9: // SelectDropdown
                            System.out.println("Select option : " + seleniumAction.getInput()
                                    + " in dropdown " + seleniumAction.getObject());

                            WebElement selectElement;

                            try {
                                // premièrement : trouver le dropdown
                                selectElement = driver.findElement(By.name(seleniumAction.getObject()));
                            } catch (NoSuchElementException ex) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "Le dropdown'" + seleniumAction.getObject() + "' est introuvable.");
                            }

                            try {
                                // ensuite : sélectionner l'option
                                Select select = new Select(selectElement);
                                select.selectByVisibleText(seleniumAction.getInput());
                            } catch (NoSuchElementException ex) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "l'Option '" + seleniumAction.getInput()
                                                + "' n'est pas trouvée dans le dropdown '" + seleniumAction.getObject()
                                                + "'");
                            } catch (Exception ex) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "Erreur lors de la sélection du dropdown: " + seleniumAction.getObject());
                            }
                            break;

                        case 10: // HoverOver
                            System.out.println("Hovering over element: " + seleniumAction.getObject());

                            WebElement hoverElement;

                            try {
                                // Step 1: find the element
                                hoverElement = driver.findElement(By.name(seleniumAction.getObject()));
                            } catch (NoSuchElementException ex) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "l'Element '" + seleniumAction.getObject() + "' est introuvable pour hover.");
                            }

                            try {
                                // Step 2: perform hover
                                new Actions(driver)
                                        .moveToElement(hoverElement)
                                        .pause(Duration.ofMillis(500))
                                        .perform();
                            } catch (Exception ex) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "Erreur lors du hover sur l'élément '" + seleniumAction.getObject());
                            }

                            break;

                        case 11: // ToggleCheckbox
                            System.out.println("Basculement de la case à cocher : "
                                    + seleniumAction.getObject() + " vers " + seleniumAction.getInput());

                            WebElement checkbox;

                            try {
                                // Étape 1 : rechercher la case à cocher
                                checkbox = driver.findElement(By.name(seleniumAction.getObject()));

                            } catch (NoSuchElementException ex) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "Case à cocher '" + seleniumAction.getObject() + "' introuvable.");
                            }

                            try {
                                System.out.println(
                                        "Found checkbox: " + seleniumAction.getObject() + ", determining state...");

                                // Étape 2 : déterminer l'état souhaité
                                boolean doitÊtreCochée = "check".equalsIgnoreCase(seleniumAction.getInput());
                                boolean estCochée = checkbox.isSelected();

                                // Étape 3 : cliquer uniquement si nécessaire
                                if (estCochée != doitÊtreCochée) {
                                    try {
                                        // Essayer un clic normal
                                        checkbox.click();
                                    } catch (org.openqa.selenium.ElementNotInteractableException e) {
                                        // Si échec, clic via JavaScript
                                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkbox);
                                    }
                                }

                                // Étape 4 : vérifier que l'état a bien changé
                                if (checkbox.isSelected() != doitÊtreCochée) {
                                    return finalizeTest(driver, seleniumResponse, startTime, false,
                                            "La case à cocher '" + seleniumAction.getObject()
                                                    + "' n’a pas été mise à jour correctement.");
                                }

                            } catch (Exception ex) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "Échec lors du basculement de la case à cocher '"
                                                + seleniumAction.getObject() + "' - " + ex.getMessage());
                            }

                            break;

                        case 12: // SelectRadio
                            WebElement RadioButton;

                            try {
                                // Étape 1 : rechercher la case à cocher
                                RadioButton = driver.findElement(By.name(seleniumAction.getObject()));
                            } catch (NoSuchElementException ex) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "Case à cocher '" + seleniumAction.getObject() + "' introuvable.");
                            }

                            try {
                                // Étape 2 : déterminer l'état souhaité
                                boolean doitEtreCochee = "check".equalsIgnoreCase(seleniumAction.getInput());
                                boolean estCochee = RadioButton.isSelected();

                                // Étape 3 : cliquer uniquement si nécessaire
                                if (estCochee != doitEtreCochee) {
                                    try {
                                        // Clic normal
                                        RadioButton.click();
                                    } catch (org.openqa.selenium.ElementNotInteractableException e) {
                                        // Fallback JavaScript si l'élément n'est pas cliquable
                                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", RadioButton);
                                    }
                                }

                                // Étape 4 : vérifier que l'état a bien changé
                                if (RadioButton.isSelected() != doitEtreCochee) {
                                    return finalizeTest(driver, seleniumResponse, startTime, false,
                                            "La case à cocher '" + seleniumAction.getObject()
                                                    + "' n’a pas été mise à jour correctement.");
                                }

                            } catch (Exception ex) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "Échec lors du basculement de la case à cocher '" + seleniumAction.getObject()
                                                + "' - " + ex.getMessage());
                            }
                            break;
                        case 13: // File upload
                            try {
                                System.out.println("Upload file : " + seleniumAction.getInput() + " to field "
                                        + seleniumAction.getObject());
                                File file = new File(seleniumAction.getInput());
                                if (!file.exists()) {
                                    return finalizeTest(driver, seleniumResponse, startTime, false,
                                            "File not found: " + seleniumAction.getInput());
                                }
                                WebElement fileUploadElement = driver.findElement(By.name(seleniumAction.getObject()));
                                fileUploadElement.sendKeys(file.getAbsolutePath());
                            } catch (Exception ex) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "Failed to upload file: " + seleniumAction.getObject() + " - "
                                                + ex.getMessage());
                            }
                            break;
                        case 14: // JS alert
                            try {
                                System.out.println("Accepting JavaScript alert.");
                                new WebDriverWait(driver, Duration.ofSeconds(5))
                                        .until(ExpectedConditions.alertIsPresent()).accept();
                            } catch (NoAlertPresentException | TimeoutException ex) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "No alert found to accept or timeout.");
                            } catch (Exception ex) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "Error handling JavaScript alert: " + ex.getMessage());
                            }
                            break;
                        case 15: // Generic input (similar to FillField)
                            try {
                                System.out.println("Generic input action on : " + seleniumAction.getObject()
                                        + " with " + seleniumAction.getInput());
                                WebElement genericInput = driver.findElement(By.name(seleniumAction.getObject()));
                                genericInput.clear();
                                genericInput.sendKeys(seleniumAction.getInput());
                            } catch (Exception ex) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "Failed to perform generic input on: " + seleniumAction.getObject() + " - "
                                                + ex.getMessage());
                            }
                            break;
                        case 16: // Redirect link (similaire à goToUrl)
                            try {
                                System.out.println("Redirecting to : " + seleniumAction.getTarget());
                                driver.get(seleniumAction.getTarget());

                                new WebDriverWait(driver, Duration.ofSeconds(10))
                                        .until(ExpectedConditions.urlContains(seleniumAction.getTarget()));

                                String currentUrl = driver.getCurrentUrl();
                                if (!currentUrl.contains(seleniumAction.getTarget())) {
                                    return finalizeTest(driver, seleniumResponse, startTime, false,
                                            "Redirection failed. Current URL: " + currentUrl);
                                }
                            } catch (Exception ex) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "Error redirecting to URL: " + seleniumAction.getTarget() + " - "
                                                + ex.getMessage());
                            }
                            break;

                        case 17: // CallCase (modularité)
                            System.out.println("Calling sub-scenario with ID : " + seleniumAction.getTarget()
                                    + " (Requires DB/Repository for full implementation)");
                            break;

                        default:
                            System.out.println("action type id : " + seleniumAction.getAction_type_id() + " not found");
                            break;
                    }
                }

                return finalizeTest(driver, seleniumResponse, startTime, true, null);

            }

            // CATCH INTERNE : Gère les erreurs survenant pendant la boucle d'actions
            catch (Exception e) {
                return finalizeTest(driver, seleniumResponse, startTime, false,
                        "Test failed during action execution: " + e.getMessage());
            }

        }

        catch (Exception e) {
            System.out.println(e);
            return finalizeTest(driver, seleniumResponse, startTime, false,
                    "Test initialisation failed: " + e.toString());
        }
    }

    // Helper method to replace localhost/relative URLs with the selected UI instance
    private String normalizeUrl(String inputUrl, String selectedUiUrl) {
        if (inputUrl == null || inputUrl.isEmpty()) {
            return inputUrl;
        }
        
        // Remplacer localhost par l'instance UI sélectionnée
        if (inputUrl.contains("localhost:4200") || inputUrl.contains("localhost:80")) {
            return inputUrl.replaceAll("localhost:\\d+", selectedUiUrl.replaceAll("https?://", ""));
        }
        
        // Si l'URL est relative (commence par /), ajouter l'instance UI
        if (inputUrl.startsWith("/")) {
            return selectedUiUrl + inputUrl;
        }
        
        return inputUrl;
    }

    // finalizeTest
    private SeleniumResponse finalizeTest(
            WebDriver driver,
            SeleniumResponse response,
            long startTime,
            boolean success,
            String output) {

        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                // TODO : Logger l’erreur si nécessaire
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;

        response.setDuration(totalTime);
        response.setSuccess(success);
        if (!success && output != null) {
            response.setOutput(output);
        }
        return response;
    }

    @GetMapping("/ui-instance")
    public String getUiInstance() {
        return uiInstanceProvider.getNextUiUrl();
    }

    @GetMapping("/all")
    public String allAccess() {
        return "Bienvenue au TAF.";
    }
}