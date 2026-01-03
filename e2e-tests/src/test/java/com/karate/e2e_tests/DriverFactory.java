package com.karate.e2e_tests;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.time.Duration;

public class DriverFactory {

    public static WebDriver create() {
        String seleniumUrl = System.getenv().getOrDefault("SELENIUM_URL", "http://localhost:4444");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");

        try {
            WebDriver driver = new RemoteWebDriver(new URL(seleniumUrl), options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            return driver;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create RemoteWebDriver for: " + seleniumUrl, e);
        }
    }
}