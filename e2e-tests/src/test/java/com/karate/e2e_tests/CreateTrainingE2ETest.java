package com.karate.e2e_tests;

import com.karate.e2e_tests.pages.LoginPage;
import com.karate.e2e_tests.pages.SelectClubPage;
import com.karate.e2e_tests.support.TestBase;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CreateTrainingE2ETest extends TestBase {

    @Test
    void adminCanCreateSingleTrainingAndSeeItInRecentList() {
        String admin = System.getenv().getOrDefault("ADMIN_USERNAME", "Admin");
        String pass = System.getenv().getOrDefault("ADMIN_PASSWORD", "Password");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime start = LocalDateTime.now().plusHours(2).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(1);

        String desc = "Some Training Description " + System.currentTimeMillis();

        LoginPage login = home().clickLoginInNavbar();
        SelectClubPage club = login.asSelectClubIfRedirected();
        if (club != null) login = club.selectLodz();

        login.login(admin, pass)
                .assertLoggedIn()
                .assertAdminLeftbar()
                .goToCreateTraining()
                .assertLoaded()
                .createSingle(desc, start.format(fmt), end.format(fmt))
                .assertInRecentList(desc);
    }
}
