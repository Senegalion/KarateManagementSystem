package com.karate.e2e_tests.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import static org.junit.jupiter.api.Assertions.*;

public class CalendarPage extends BasePage {

    private final By h1 = By.xpath("//h1[contains(normalize-space(),'Training Calendar')]");
    private final By prev = By.xpath("//button[contains(normalize-space(),'Previous')]");
    private final By next = By.xpath("//button[contains(normalize-space(),'Next')]");
    private final By monthHeader = By.xpath("//h2[contains(normalize-space(),'2026')]");

    public CalendarPage(WebDriver driver) {
        super(driver);
        wait.until(d -> d.getCurrentUrl() != null && d.getCurrentUrl().contains("/app/calendar"));
    }

    public CalendarPage assertLoaded() {
        $(h1);
        $(prev);
        $(next);
        $(monthHeader);

        for (String day : new String[]{"Sun","Mon","Tue","Wed","Thu","Fri","Sat"}) {
            By dayHdr = By.xpath("//*[contains(@class,'uppercase') and normalize-space()='" + day + "']");
            assertTrue(exists(dayHdr), "Missing weekday header: " + day);
        }

        int tiles = driver.findElements(By.cssSelector("div.cursor-pointer")).size();
        assertTrue(tiles >= 28, "Expected >= 28 day tiles, got: " + tiles);
        return this;
    }
}
