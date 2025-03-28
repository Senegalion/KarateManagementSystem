package com.karate.management.karatemanagementsystem.infrastructure.scheduler;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.karate.management.karatemanagementsystem.model.entity.PaymentEntity;
import com.karate.management.karatemanagementsystem.model.entity.UserEntity;
import com.karate.management.karatemanagementsystem.model.repository.PaymentRepository;
import com.karate.management.karatemanagementsystem.model.repository.UserRepository;
import com.karate.management.karatemanagementsystem.model.staticdata.PaymentStatus;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaymentReminderSchedulerIT {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentReminderScheduler paymentReminderScheduler;

    @MockBean
    private JavaMailSender mailSender;

    private static GreenMail greenMail;

    @BeforeAll
    static void startMailServer() {
        greenMail = new GreenMail(new ServerSetup(3025, null, "smtp"));
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
    void testSendPaymentReminders() {
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

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // when
        paymentReminderScheduler.sendPaymentReminders();

        // then
        List<YearMonth> unpaidMonths = paymentService.getUnpaidMonthsForUser(user);
        assertFalse(unpaidMonths.isEmpty(), "User should have unpaid months");

        verify(mailSender, atLeastOnce()).send(any(MimeMessage.class));
    }
}