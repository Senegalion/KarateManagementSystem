package com.karate.e2e_tests.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;
    protected final String baseUrl;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        this.baseUrl = System.getenv().getOrDefault("BASE_URL", "http://frontend");
    }

    protected WebElement $(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    protected WebElement clickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void click(By locator) {
        clickable(locator).click();
    }

    protected boolean exists(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    protected void type(By locator, String value) {
        WebElement el = clickable(locator);
        el.sendKeys(Keys.chord(Keys.CONTROL, "a"), value);
    }

    protected void setDateTimeLocal(By locator, String value) {
        WebElement input = $(locator);
        ((JavascriptExecutor) driver).executeScript("""
                    const el = arguments[0];
                    const val = arguments[1];
                    el.value = val;
                    el.dispatchEvent(new Event('input', { bubbles: true }));
                    el.dispatchEvent(new Event('change', { bubbles: true }));
                    el.blur();
                """, input, value);
    }
}
