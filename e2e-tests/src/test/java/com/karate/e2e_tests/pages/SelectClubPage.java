package com.karate.e2e_tests.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class SelectClubPage extends BasePage {

    private final By header = By.xpath("//h1[normalize-space()='Select Karate Club']");
    private final By lodzTile = By.xpath("//span[contains(normalize-space(),'Łódź Okinawa Shorin Ryu Karate')]/ancestor::div[contains(@class,'cursor-pointer')]");

    public SelectClubPage(WebDriver driver) {
        super(driver);
        $(header);
    }

    public LoginPage selectLodz() {
        click(lodzTile);
        return new LoginPage(driver);
    }
}
