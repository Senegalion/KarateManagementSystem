package com.karate.management.karatemanagementsystem.infrastructure.scheduler;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.karate.management.karatemanagementsystem.payment.domain.model.PaymentEntity;
import com.karate.management.karatemanagementsystem.user.domain.model.UserEntity;
import com.karate.management.karatemanagementsystem.payment.domain.repository.PaymentRepository;
import com.karate.management.karatemanagementsystem.user.domain.repository.UserRepository;
import com.karate.management.karatemanagementsystem.payment.domain.model.PaymentStatus;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EmailSenderIT {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private static GreenMail greenMail;

    @BeforeAll
    static void startMailServer() {
        greenMail = new GreenMail(new ServerSetup(3025, null, "smtp")).withConfiguration(GreenMailConfiguration.aConfig().withDisabledAuthentication());
        greenMail.start();
    }

    @AfterAll
    static void stopMailServer() {
        greenMail.stop();
    }

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        paymentRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void testSchedulerSendsEmails() {
        // given
        UserEntity user = new UserEntity();
        user.setUsername("testUser");
        user.setEmail("someEmail@gmail.com");
        user.setPassword("password");
        user.setRegistrationDate(LocalDate.of(2023, 1, 1));
        user = userRepository.save(user);

        PaymentEntity payment = new PaymentEntity();
        payment.setUserEntity(user);
        payment.setPaymentDate(LocalDate.of(2023, 2, 1));
        payment.setPaymentStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);

        // when && then
        await()
                .atMost(15, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
                    assertTrue(receivedMessages.length > 0, "At least one email should be sent");

                    String subject = receivedMessages[0].getSubject();
                    assertTrue(subject.contains("Payment Reminder: Outstanding Balance"), "Email should contain 'Payment Reminder'");
                });
    }
}
