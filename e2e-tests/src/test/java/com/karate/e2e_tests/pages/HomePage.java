package com.karate.e2e_tests.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HomePage extends BasePage {

    private final By loginLink = By.cssSelector("a[href='/login']");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public HomePage open() {
        driver.get(baseUrl + "/");
        wait.until(d -> d.getTitle() != null && d.getTitle().contains("Karate"));
        return this;
    }

    public LoginPage clickLoginInNavbar() {
        click(loginLink);
        return new LoginPage(driver);
    }

    public void assertLoginVisible() {
        assertTrue(exists(loginLink), "Expected Login link in navbar");
    }
}
