package com.karate.e2e_tests.support;

import com.karate.e2e_tests.DriverFactory;
import com.karate.e2e_tests.pages.HomePage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;

public abstract class TestBase {

    protected WebDriver driver;

    @BeforeEach
    void baseSetUp() {
        driver = DriverFactory.create();
    }

    @AfterEach
    void baseTearDown() {
        if (driver != null) driver.quit();
    }

    protected HomePage home() {
        return new HomePage(driver).open();
    }
}
