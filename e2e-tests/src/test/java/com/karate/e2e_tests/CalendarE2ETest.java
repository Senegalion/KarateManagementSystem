package com.karate.e2e_tests;

import com.karate.e2e_tests.pages.AppShell;
import com.karate.e2e_tests.pages.LoginPage;
import com.karate.e2e_tests.pages.SelectClubPage;
import com.karate.e2e_tests.support.TestBase;
import org.junit.jupiter.api.Test;

public class CalendarE2ETest extends TestBase {

    @Test
    void userCanOpenCalendarFromDashboard() {
        String user = System.getenv().getOrDefault("USER_USERNAME", "User2");
        String pass = System.getenv().getOrDefault("USER_PASSWORD", "Password");

        LoginPage login = home().clickLoginInNavbar();
        SelectClubPage club = login.asSelectClubIfRedirected();
        if (club != null) login = club.selectLodz();

        AppShell app = login.login(user, pass).assertLoggedIn();

        app.goToDashboard()
                .openCalendar()
                .assertLoaded();
    }

    @Test
    void adminCanOpenCalendarFromLeftbar() {
        String user = System.getenv().getOrDefault("ADMIN_USERNAME", "Admin");
        String pass = System.getenv().getOrDefault("ADMIN_PASSWORD", "Password");

        LoginPage login = home().clickLoginInNavbar();
        SelectClubPage club = login.asSelectClubIfRedirected();
        if (club != null) login = club.selectLodz();

        login.login(user, pass)
                .assertLoggedIn()
                .goToCalendar()
                .assertLoaded();
    }
}
