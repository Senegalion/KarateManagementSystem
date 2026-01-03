package com.karate.e2e_tests.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {

    private final By form = By.cssSelector("form");
    private final By username = By.cssSelector("form input[type='text']");
    private final By password = By.cssSelector("form input[type='password']");
    private final By loginBtn = By.xpath("//form//button[normalize-space()='Login']");
    private final By selectClubHeader = By.xpath("//h1[normalize-space()='Select Karate Club']");

    public LoginPage(WebDriver driver) {
        super(driver);
        wait.until(d -> exists(form) || exists(selectClubHeader));
    }

    public SelectClubPage asSelectClubIfRedirected() {
        if (exists(selectClubHeader)) return new SelectClubPage(driver);
        return null;
    }

    public AppShell login(String user, String pass) {
        if (!exists(form) && exists(selectClubHeader)) {
            throw new IllegalStateException("On select-club, not on login form. Call SelectClubPage first.");
        }

        type(username, user);
        type(password, pass);
        click(loginBtn);

        return new AppShell(driver);
    }
}
