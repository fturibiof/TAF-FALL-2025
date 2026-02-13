package ca.etsmtl.selenium.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Pool of reusable WebDriver instances for parallel test execution.
 * Dramatically reduces test execution time by reusing browser instances.
 */
@Component
public class WebDriverPool {
    private static final Logger logger = LoggerFactory.getLogger(WebDriverPool.class);
    
    private static final int POOL_SIZE = 5; // Number of concurrent browsers
    private static final int ACQUIRE_TIMEOUT_SECONDS = 60;
    
    private final BlockingQueue<WebDriver> availableDrivers = new LinkedBlockingQueue<>();
    private final List<WebDriver> allDrivers = new ArrayList<>();
    private volatile boolean initialized = false;
    
    /**
     * Get a WebDriver from the pool (or create new ones if pool is empty)
     */
    public WebDriver acquireDriver() throws InterruptedException {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    initializePool();
                    initialized = true;
                }
            }
        }
        
        WebDriver driver = availableDrivers.poll(ACQUIRE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (driver == null) {
            throw new RuntimeException("Timeout waiting for available WebDriver from pool");
        }
        
        logger.debug("WebDriver acquired from pool. Available: {}", availableDrivers.size());
        return driver;
    }
    
    /**
     * Return a WebDriver to the pool for reuse
     */
    public void releaseDriver(WebDriver driver) {
        if (driver != null) {
            try {
                // Clean up before returning to pool
                driver.manage().deleteAllCookies();
                availableDrivers.offer(driver);
                logger.debug("WebDriver returned to pool. Available: {}", availableDrivers.size());
            } catch (Exception e) {
                logger.error("Error cleaning up driver, creating replacement", e);
                try {
                    driver.quit();
                } catch (Exception ignored) {}
                
                // Create a replacement driver
                try {
                    WebDriver newDriver = createDriver();
                    allDrivers.add(newDriver);
                    availableDrivers.offer(newDriver);
                } catch (Exception ex) {
                    logger.error("Failed to create replacement driver", ex);
                }
            }
        }
    }
    
    /**
     * Initialize the pool with WebDriver instances
     */
    private void initializePool() {
        logger.info("Initializing WebDriver pool with {} instances", POOL_SIZE);
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < POOL_SIZE; i++) {
            try {
                WebDriver driver = createDriver();
                allDrivers.add(driver);
                availableDrivers.offer(driver);
                logger.info("Created WebDriver instance {}/{}", i + 1, POOL_SIZE);
            } catch (Exception e) {
                logger.error("Failed to create WebDriver instance {}", i + 1, e);
            }
        }
        
        long duration = System.currentTimeMillis() - startTime;
        logger.info("WebDriver pool initialized with {} instances in {}ms", 
                    availableDrivers.size(), duration);
    }
    
    /**
     * Create a new WebDriver instance with optimized settings
     */
    private WebDriver createDriver() {
        String[] possibleChromePaths = {
            "/usr/bin/google-chrome",
            "/usr/bin/google-chrome-stable",
            "/usr/bin/chromium",
            "/usr/bin/chromium-browser",
            "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
            "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe"
        };

        String chromePath = null;
        for (String path : possibleChromePaths) {
            if (new java.io.File(path).exists()) {
                chromePath = path;
                break;
            }
        }

        if (chromePath == null) {
            logger.warn("Chrome binary not found, letting ChromeDriver find it automatically");
        }

        ChromeOptions options = new ChromeOptions();
        if (chromePath != null) {
            options.setBinary(chromePath);
        }
        options.addArguments("--no-sandbox");
        options.addArguments("--headless=new");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--window-size=1920x1080");
        options.addArguments("--disable-software-rasterizer");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-logging");
        options.addArguments("--log-level=3");
        
        return new ChromeDriver(options);
    }
    
    /**
     * Cleanup: Quit all WebDriver instances
     */
    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down WebDriver pool. Total drivers: {}", allDrivers.size());
        
        for (WebDriver driver : allDrivers) {
            try {
                driver.quit();
            } catch (Exception e) {
                logger.error("Error quitting driver during shutdown", e);
            }
        }
        
        allDrivers.clear();
        availableDrivers.clear();
        logger.info("WebDriver pool shutdown complete");
    }
}
