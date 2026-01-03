package com.karate.e2e_tests.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class DashboardPage extends BasePage {

    private final By openCalendarBtn = By.xpath("//a[@href='/app/calendar' and contains(normalize-space(),'Open Calendar')]");

    public DashboardPage(WebDriver driver) {
        super(driver);
        wait.until(d -> d.getCurrentUrl() != null && d.getCurrentUrl().contains("/app/dashboard"));
    }

    public CalendarPage openCalendar() {
        click(openCalendarBtn);
        return new CalendarPage(driver);
    }
}
