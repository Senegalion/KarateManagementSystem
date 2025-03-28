package com.karate.management.karatemanagementsystem.infrastructure.scheduler;

import com.karate.management.karatemanagementsystem.model.entity.PaymentEntity;
import com.karate.management.karatemanagementsystem.model.entity.UserEntity;
import com.karate.management.karatemanagementsystem.model.repository.PaymentRepository;
import com.karate.management.karatemanagementsystem.model.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

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
        List<YearMonth> paidMonths = paymentRepository.findPaymentsByUserEntityUserId(userDto.getUserId()).stream()
                .map(PaymentEntity::getPaymentDate)
                .map(YearMonth::from)
                .toList();

        List<YearMonth> unpaidMonths = new ArrayList<>();
        YearMonth startMonth = currentMonth.minusMonths(2);
        YearMonth endMonth = currentMonth.plusMonths(1);

        for (YearMonth month = startMonth; !month.isAfter(endMonth); month = month.plusMonths(1)) {
            if (!paidMonths.contains(month)) {
                unpaidMonths.add(month);
            }
        }

        return unpaidMonths;
    }

    public double calculateTotalDebt(UserEntity user) {
        return getUnpaidMonthsForUser(user).size() * 150.00;
    }
}
