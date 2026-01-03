package com.karate.e2e_tests.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import static org.junit.jupiter.api.Assertions.*;

public class CreateTrainingPage extends BasePage {

    private final By h1 = By.xpath("//h1[contains(normalize-space(),'Create New Training')]");
    private final By desc = By.id("description");
    private final By start = By.id("startTime");
    private final By end = By.id("endTime");
    private final By submit = By.xpath("//button[@type='submit' and contains(normalize-space(),'Create Training')]");

    private final By recentLis = By.xpath("//h2[contains(normalize-space(),'Recently Created Trainings')]/following-sibling::ul[1]/li");

    public CreateTrainingPage(WebDriver driver) {
        super(driver);
        wait.until(d -> d.getCurrentUrl() != null && d.getCurrentUrl().contains("/app/trainings/new"));
    }

    public CreateTrainingPage assertLoaded() {
        $(h1);
        $(desc);
        $(start);
        $(end);
        assertTrue(exists(By.xpath("//h2[contains(normalize-space(),'Recently Created Trainings')]")));
        return this;
    }

    public CreateTrainingPage createSingle(String description, String startVal, String endVal) {
        int before = driver.findElements(recentLis).size();

        type(desc, description);
        setDateTimeLocal(start, startVal);
        setDateTimeLocal(end, endVal);

        assertEquals(startVal, $(start).getAttribute("value"), "startTime not set");
        assertEquals(endVal, $(end).getAttribute("value"), "endTime not set");

        click(submit);

        wait.until(d -> d.findElements(recentLis).size() > before || exists(recentItemByDescription(description)));
        return this;
    }

    public CreateTrainingPage assertInRecentList(String description) {
        assertTrue(exists(recentItemByDescription(description)),
                "New training not found in recent list: " + description);
        return this;
    }

    private By recentItemByDescription(String description) {
        return By.xpath("//h2[contains(normalize-space(),'Recently Created Trainings')]/following-sibling::ul[1]/li[contains(.,'" + description + "')]");
    }
}
