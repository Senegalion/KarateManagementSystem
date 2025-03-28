package com.karate.management.karatemanagementsystem.infrastructure.scheduler;

import com.karate.management.karatemanagementsystem.model.dto.user.scheduler.UserWithDebtDto;
import com.karate.management.karatemanagementsystem.model.entity.UserEntity;
import com.karate.management.karatemanagementsystem.service.mapper.UserMapper;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class PaymentReminderScheduler {
    private final PaymentService paymentService;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 8 10 * ?")
    public void sendPaymentReminders() {
        List<UserEntity> usersWithDebt = paymentService.getUsersWithOutstandingPayments();

        for (UserEntity user : usersWithDebt) {
            List<YearMonth> unpaidMonths = paymentService.getUnpaidMonthsForUser(user);
            double totalDebt = paymentService.calculateTotalDebt(user);
            UserWithDebtDto userDto = UserMapper.convertToUserWithDebtDto(user);

            String subject = "Przypomnienie o zaległych płatnościach";
            String message = buildEmailMessage(userDto, unpaidMonths, totalDebt);

            emailService.sendEmail(userDto.email(), subject, message);
        }
    }

    private String buildEmailMessage(UserWithDebtDto user, List<YearMonth> unpaidMonths, double totalDebt) {
        String monthsList = unpaidMonths.stream()
                .map(YearMonth::toString)
                .collect(Collectors.joining(", "));

        return String.format(
                "Drogi %s,\n\n" +
                        "Przypominamy, że masz zaległe płatności za następujące miesiące: %s.\n" +
                        "Łączna kwota do uregulowania: %.2f PLN.\n\n" +
                        "Prosimy o dokonanie płatności najszybciej jak to możliwe.\n\n" +
                        "Pozdrawiamy,\nZespół Karate Management System",
                user.username(), monthsList, totalDebt
        );
    }
}
