package com.karate.e2e_tests.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.*;

public class AppShell extends BasePage {

    private final By aside = By.cssSelector("aside");
    private final By loginLink = By.cssSelector("a[href='/login']");

    private final By calendarLink = By.cssSelector("a[href='/app/calendar']");
    private final By createTrainingLink = By.cssSelector("a[href='/app/trainings/new']");

    public AppShell(WebDriver driver) {
        super(driver);
        wait.until(d -> d.getCurrentUrl() != null && d.getCurrentUrl().contains("/app") || exists(aside));
        $(aside);
    }

    public AppShell assertLoggedIn() {
        wait.until(d -> d.findElements(loginLink).isEmpty());
        assertTrue(driver.findElements(loginLink).isEmpty(), "Login link still visible -> login likely failed");
        return this;
    }

    public AppShell assertAdminLeftbar() {
        WebElement bar = $(aside);
        String t = bar.getText();

        assertTrue(t.contains("Dashboard"));
        assertTrue(t.contains("Calendar"));
        assertTrue(t.contains("My trainings"));
        assertTrue(t.contains("My feedbacks"));
        assertTrue(t.contains("My payments"));

        assertTrue(t.contains("Users"));
        assertTrue(t.contains("Create Training"));
        assertTrue(t.contains("Enrollments (admin)"));
        assertTrue(t.contains("Feedbacks (admin)"));
        assertTrue(t.contains("Payments (Admin)"));
        return this;
    }

    public AppShell assertUserLeftbar() {
        WebElement bar = $(aside);
        String t = bar.getText();

        assertTrue(t.contains("Dashboard"));
        assertTrue(t.contains("Calendar"));
        assertTrue(t.contains("My trainings"));
        assertTrue(t.contains("My feedbacks"));
        assertTrue(t.contains("My payments"));

        assertFalse(t.contains("Users"));
        assertFalse(t.contains("Create Training"));
        assertFalse(t.contains("Enrollments (admin)"));
        assertFalse(t.contains("Feedbacks (admin)"));
        assertFalse(t.contains("Payments (Admin)"));
        return this;
    }

    public CalendarPage goToCalendar() {
        click(calendarLink);
        return new CalendarPage(driver);
    }

    public CreateTrainingPage goToCreateTraining() {
        click(createTrainingLink);
        return new CreateTrainingPage(driver);
    }

    public DashboardPage goToDashboard() {
        click(By.cssSelector("a[href='/app/dashboard']"));
        return new DashboardPage(driver);
    }
}
