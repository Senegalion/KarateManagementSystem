package com.karate.management.karatemanagementsystem.infrastructure.scheduler;

import com.karate.management.karatemanagementsystem.model.dto.user.scheduler.UserWithDebtDto;
import com.karate.management.karatemanagementsystem.model.entity.UserEntity;
import com.karate.management.karatemanagementsystem.service.mapper.UserMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
@Slf4j
public class PaymentReminderScheduler {
    private final PaymentService paymentService;
    private final EmailService emailService;

    @Scheduled(cron = "${scheduler.cron}")
    public void sendPaymentReminders() {
        List<UserEntity> usersWithDebt = paymentService.getUsersWithOutstandingPayments();

        if (usersWithDebt.isEmpty()) {
            log.info("No users with outstanding payments.");
            return;
        }

        for (UserEntity user : usersWithDebt) {
            List<YearMonth> unpaidMonths = paymentService.getUnpaidMonthsForUser(user);
            if (unpaidMonths.isEmpty()) {
                log.info("User {} has no unpaid months, skipping reminder.", user.getUsername());
                continue;
            }

            double totalDebt = paymentService.calculateTotalDebt(user);
            UserWithDebtDto userDto = UserMapper.convertToUserWithDebtDto(user);

            String subject = "Payment Reminder: Outstanding Balance";
            String message = buildEmailMessage(userDto, unpaidMonths, totalDebt);

            emailService.sendEmail(userDto.email(), subject, message);
            log.info("Payment reminder sent to {} for months: {} with total debt: {}.", user.getUsername(), unpaidMonths, totalDebt);
        }
    }

    private String buildEmailMessage(UserWithDebtDto user, List<YearMonth> unpaidMonths, double totalDebt) {
        String monthsList = unpaidMonths.stream()
                .map(YearMonth::toString)
                .collect(Collectors.joining(", "));

        return String.format(
                "Dear %s,\n\n" +
                        "This is a reminder that you have outstanding payments for the following months: %s.\n" +
                        "Total amount due: %.2f PLN.\n\n" +
                        "Please make your payment as soon as possible.\n\n" +
                        "Best regards,\nKarate Management System Team",
                user.username(), monthsList, totalDebt
        );
    }
}
