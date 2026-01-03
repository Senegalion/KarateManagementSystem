package com.karate.e2e_tests;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class E2EFlows {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String baseUrl;

    public E2EFlows(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        this.baseUrl = System.getenv().getOrDefault("BASE_URL", "http://frontend");
    }

    public E2EFlows openHome() {
        driver.get(baseUrl + "/");
        wait.until(d -> Objects.requireNonNull(d.getTitle()).contains("Karate"));
        return this;
    }

    public E2EFlows goToLoginFromNavbar() {
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/login']"))).click();

        wait.until(d ->
                Objects.requireNonNull(d.getCurrentUrl()).contains("/login")
                        || d.getCurrentUrl().contains("/select-club")
                        || !d.findElements(By.cssSelector("form")).isEmpty()
                        || !d.findElements(By.xpath("//h1[normalize-space()='Select Karate Club']")).isEmpty()
        );
        return this;
    }

    public E2EFlows selectLodzClubIfNeeded() {
        if (!Objects.requireNonNull(driver.getCurrentUrl()).contains("/select-club")) return this;

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//h1[normalize-space()='Select Karate Club']")
        ));

        By lodzSpan = By.xpath("//span[contains(normalize-space(),'Łódź Okinawa Shorin Ryu Karate')]");
        WebElement span = wait.until(ExpectedConditions.presenceOfElementLocated(lodzSpan));

        WebElement tile = span.findElement(By.xpath("./ancestor::div[contains(@class,'cursor-pointer')]"));
        wait.until(ExpectedConditions.elementToBeClickable(tile)).click();

        wait.until(d -> Objects.requireNonNull(d.getCurrentUrl()).contains("/login")
                || !d.findElements(By.cssSelector("form")).isEmpty());
        return this;
    }

    public E2EFlows login(String username, String password) {
        WebElement form = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("form")));

        WebElement usernameInput = wait.until(d -> form.findElement(By.cssSelector("input[type='text']")));
        WebElement passwordInput = wait.until(d -> form.findElement(By.cssSelector("input[type='password']")));

        usernameInput.sendKeys(Keys.chord(Keys.CONTROL, "a"), username);
        passwordInput.sendKeys(Keys.chord(Keys.CONTROL, "a"), password);

        WebElement loginButton = wait.until(d -> form.findElement(By.xpath(".//button[normalize-space()='Login']")));
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();
        return this;
    }

    public E2EFlows assertLoggedInByLoginLinkDisappears() {
        wait.until(d -> d.findElements(By.cssSelector("a[href='/login']")).isEmpty());
        assertTrue(driver.findElements(By.cssSelector("a[href='/login']")).isEmpty(),
                "Login link still visible - login likely failed.");
        return this;
    }

    public void assertSelectClubHasTiles() {
        driver.get(baseUrl + "/select-club?redirect=%2Flogin");
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//h1[normalize-space()='Select Karate Club']")
        ));
        assertFalse(driver.findElements(By.cssSelector("div.cursor-pointer")).isEmpty(),
                "Expected at least one club tile (div.cursor-pointer) on select-club page.");
    }

    public void assertAdminLeftbarVisible() {
        WebElement aside = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("aside")));

        assertTrue(aside.getText().contains("Dashboard"), "Missing Dashboard in admin leftbar");
        assertTrue(aside.getText().contains("Calendar"), "Missing Calendar in admin leftbar");
        assertTrue(aside.getText().contains("My trainings"), "Missing My trainings in admin leftbar");
        assertTrue(aside.getText().contains("My feedbacks"), "Missing My feedbacks in admin leftbar");
        assertTrue(aside.getText().contains("My payments"), "Missing My payments in admin leftbar");

        assertTrue(aside.getText().contains("Users"), "Missing Users in admin leftbar");
        assertTrue(aside.getText().contains("Create Training"), "Missing Create Training in admin leftbar");
        assertTrue(aside.getText().contains("Enrollments (admin)"), "Missing Enrollments (admin) in admin leftbar");
        assertTrue(aside.getText().contains("Feedbacks (admin)"), "Missing Feedbacks (admin) in admin leftbar");
        assertTrue(aside.getText().contains("Payments (Admin)"), "Missing Payments (Admin) in admin leftbar");
    }

    public void assertUserLeftbarVisible() {
        WebElement aside = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("aside")));

        assertTrue(aside.getText().contains("Dashboard"), "Missing Dashboard in user leftbar");
        assertTrue(aside.getText().contains("Calendar"), "Missing Calendar in user leftbar");
        assertTrue(aside.getText().contains("My trainings"), "Missing My trainings in user leftbar");
        assertTrue(aside.getText().contains("My feedbacks"), "Missing My feedbacks in user leftbar");
        assertTrue(aside.getText().contains("My payments"), "Missing My payments in user leftbar");

        assertFalse(aside.getText().contains("Users"), "User leftbar should NOT contain Users");
        assertFalse(aside.getText().contains("Create Training"), "User leftbar should NOT contain Create Training");
        assertFalse(aside.getText().contains("Enrollments (admin)"), "User leftbar should NOT contain Enrollments (admin)");
        assertFalse(aside.getText().contains("Feedbacks (admin)"), "User leftbar should NOT contain Feedbacks (admin)");
        assertFalse(aside.getText().contains("Payments (Admin)"), "User leftbar should NOT contain Payments (Admin)");
    }

    public E2EFlows waitForAppShell() {
        wait.until(d ->
                Objects.requireNonNull(d.getCurrentUrl()).contains("/app")
                        || !d.findElements(By.cssSelector("aside")).isEmpty()
        );
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("aside")));
        return this;
    }

    public E2EFlows openCalendarFromDashboardButton() {
        By openCalendar = By.xpath("//a[@href='/app/calendar' and contains(normalize-space(),'Open Calendar')]");
        wait.until(ExpectedConditions.elementToBeClickable(openCalendar)).click();

        wait.until(d -> Objects.requireNonNull(d.getCurrentUrl()).contains("/app/calendar"));
        return this;
    }

    public E2EFlows assertOnCalendarPage() {
        wait.until(d -> Objects.requireNonNull(d.getCurrentUrl()).contains("/app/calendar"));

        By h1 = By.xpath("//h1[contains(normalize-space(),'Training Calendar')]");
        wait.until(ExpectedConditions.visibilityOfElementLocated(h1));

        By prev = By.xpath("//button[contains(normalize-space(),'Previous')]");
        By next = By.xpath("//button[contains(normalize-space(),'Next')]");
        wait.until(ExpectedConditions.visibilityOfElementLocated(prev));
        wait.until(ExpectedConditions.visibilityOfElementLocated(next));

        By monthHeader = By.xpath("//h2[contains(normalize-space(),'2026')]");
        wait.until(ExpectedConditions.visibilityOfElementLocated(monthHeader));

        for (String day : new String[]{"Sun","Mon","Tue","Wed","Thu","Fri","Sat"}) {
            By dayHdr = By.xpath("//*[contains(@class,'uppercase') and normalize-space()='" + day + "']");
            assertFalse(driver.findElements(dayHdr).isEmpty(), "Missing weekday header: " + day);
        }

        int dayTiles = driver.findElements(By.cssSelector("div.cursor-pointer")).size();
        assertTrue(dayTiles >= 28, "Expected at least 28 day tiles, got: " + dayTiles);

        return this;
    }

    public E2EFlows goToCreateTrainingFromLeftbar() {
        By link = By.cssSelector("a[href='/app/trainings/new']");
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();

        wait.until(d -> Objects.requireNonNull(d.getCurrentUrl()).contains("/app/trainings/new"));
        return this;
    }

    public E2EFlows assertCreateTrainingPageVisible() {
        By h1 = By.xpath("//h1[contains(normalize-space(),'Create New Training')]");
        wait.until(ExpectedConditions.visibilityOfElementLocated(h1));

        assertFalse(driver.findElements(By.xpath("//button[normalize-space()='Single']")).isEmpty(),
                "Missing 'Single' toggle");
        assertFalse(driver.findElements(By.xpath("//button[normalize-space()='Recurring']")).isEmpty(),
                "Missing 'Recurring' toggle");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("description")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("startTime")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("endTime")));

        By recentHeader = By.xpath("//h2[contains(normalize-space(),'Recently Created Trainings')]");
        wait.until(ExpectedConditions.visibilityOfElementLocated(recentHeader));

        return this;
    }

    public E2EFlows createSingleTraining(String description, String start, String end) {
        wait.until(d -> Objects.requireNonNull(d.getCurrentUrl()).contains("/app/trainings/new"));

        By recentListItems = By.cssSelector("ul li");
        int beforeCount = driver.findElements(recentListItems).size();

        WebElement desc = wait.until(ExpectedConditions.elementToBeClickable(By.id("description")));
        WebElement startTime = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("startTime")));
        WebElement endTime = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("endTime")));

        desc.sendKeys(Keys.chord(Keys.CONTROL, "a"), description);

        setDateTimeLocal(startTime, start);
        setDateTimeLocal(endTime, end);

        String startVal = startTime.getAttribute("value");
        String endVal = endTime.getAttribute("value");
        assertTrue(start.equals(startVal), "startTime value NOT set. expected=" + start + " actual=" + startVal);
        assertTrue(end.equals(endVal), "endTime value NOT set. expected=" + end + " actual=" + endVal);

        By submit = By.xpath("//button[@type='submit' and contains(normalize-space(),'Create Training')]");
        wait.until(ExpectedConditions.elementToBeClickable(submit)).click();

        wait.until(d -> {
            int nowCount = d.findElements(recentListItems).size();
            if (nowCount > beforeCount) return true;

            return !d.findElements(By.xpath("//ul//li[contains(.,'" + description + "')]")).isEmpty();
        });

        return this;
    }

    public E2EFlows assertTrainingAppearsInRecentList(String description) {
        By liWithDesc = By.xpath("//ul//li[contains(.,'" + description + "')]");
        assertFalse(driver.findElements(liWithDesc).isEmpty(),
                "New training not found in recent list: " + description + "\nURL=" + driver.getCurrentUrl());
        return this;
    }

    private void setDateTimeLocal(WebElement input, String value) {
        ((JavascriptExecutor) driver).executeScript("""
        const el = arguments[0];
        const val = arguments[1];
        el.value = val;
        el.dispatchEvent(new Event('input', { bubbles: true }));
        el.dispatchEvent(new Event('change', { bubbles: true }));
        el.blur();
    """, input, value);
    }

    private String escapeXpath(String text) {
        if (!text.contains("'")) return text;
        String[] parts = text.split("'");
        StringBuilder sb = new StringBuilder("concat(");
        for (int i = 0; i < parts.length; i++) {
            sb.append("'").append(parts[i]).append("'");
            if (i < parts.length - 1) sb.append(",\"'\",");
        }
        sb.append(")");
        return sb.toString();
    }
}
