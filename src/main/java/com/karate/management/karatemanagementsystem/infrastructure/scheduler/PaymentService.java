package com.karate.management.karatemanagementsystem.infrastructure.scheduler;

import com.karate.management.karatemanagementsystem.model.entity.PaymentEntity;
import com.karate.management.karatemanagementsystem.model.entity.UserEntity;
import com.karate.management.karatemanagementsystem.model.repository.PaymentRepository;
import com.karate.management.karatemanagementsystem.model.repository.UserRepository;
import com.karate.management.karatemanagementsystem.model.staticdata.PaymentStatus;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PaymentService {
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    public List<UserEntity> getUsersWithOutstandingPayments() {
        return userRepository.findAll().stream()
                .filter(user -> !getUnpaidMonthsForUser(user).isEmpty())
                .collect(Collectors.toList());
    }

    public List<YearMonth> getUnpaidMonthsForUser(UserEntity userDto) {
        YearMonth currentMonth = YearMonth.now();
        LocalDate userRegistrationDate = userDto.getRegistrationDate();

        List<YearMonth> paidMonths = paymentRepository.findPaymentsByUserEntityUserId(userDto.getUserId()).stream()
                .filter(payment -> payment.getPaymentStatus() == PaymentStatus.PAID)
                .map(PaymentEntity::getPaymentDate)
                .map(YearMonth::from)
                .collect(Collectors.toList());

        return extractUnpaidMonths(currentMonth, paidMonths, userRegistrationDate);
    }

    private static List<YearMonth> extractUnpaidMonths(YearMonth currentMonth, List<YearMonth> paidMonths, LocalDate userRegistrationDate) {
        List<YearMonth> unpaidMonths = new ArrayList<>();

        YearMonth registrationMonth = YearMonth.from(userRegistrationDate);

        for (YearMonth month = registrationMonth; !month.isAfter(currentMonth); month = month.plusMonths(1)) {
            if (!paidMonths.contains(month)) {
                unpaidMonths.add(month);
            }
        }
        return unpaidMonths;
    }

    public double calculateTotalDebt(UserEntity user) {
        List<YearMonth> unpaidMonths = getUnpaidMonthsForUser(user);
        return unpaidMonths.size() * 150.00;
    }
}
