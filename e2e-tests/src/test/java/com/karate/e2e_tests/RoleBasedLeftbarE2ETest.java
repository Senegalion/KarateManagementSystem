package com.karate.e2e_tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

public class RoleBasedLeftbarE2ETest {

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
    void adminShouldSeeAdminMenuItemsInLeftbar() {
        String adminUser = System.getenv().getOrDefault("ADMIN_USERNAME", "Admin");
        String adminPass = System.getenv().getOrDefault("ADMIN_PASSWORD", "Password");

        flows.openHome()
                .goToLoginFromNavbar()
                .selectLodzClubIfNeeded()
                .login(adminUser, adminPass)
                .assertLoggedInByLoginLinkDisappears()
                .assertAdminLeftbarVisible();
    }

    @Test
    void userShouldNotSeeAdminMenuItemsInLeftbar() {
        String userUser = System.getenv().getOrDefault("USER_USERNAME", "User2");
        String userPass = System.getenv().getOrDefault("USER_PASSWORD", "Password");

        flows.openHome()
                .goToLoginFromNavbar()
                .selectLodzClubIfNeeded()
                .login(userUser, userPass)
                .assertLoggedInByLoginLinkDisappears()
                .assertUserLeftbarVisible();
    }
}
