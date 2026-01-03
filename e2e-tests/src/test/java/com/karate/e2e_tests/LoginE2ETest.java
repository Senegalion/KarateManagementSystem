package com.karate.e2e_tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

public class LoginE2ETest {

    private WebDriver driver;
    private E2EFlows flows;

    @BeforeEach
    void setUp() {
        driver = DriverFactory.create();
        flows = new E2EFlows(driver);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    void shouldSelectLodzClubThenLoginAsAdmin() {
        flows.openHome()
                .goToLoginFromNavbar()
                .selectLodzClubIfNeeded()
                .login("Admin", "Password")
                .assertLoggedInByLoginLinkDisappears();
    }

    @Test
    void shouldRequireClubSelectionBeforeLogin() {
        flows.openHome()
                .goToLoginFromNavbar()
                .assertSelectClubHasTiles();
    }
}
