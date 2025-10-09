package com.karate.payment_service.domain.service;

import com.karate.payment_service.domain.model.PaymentItemEntity;
import com.karate.payment_service.domain.model.PaymentStatus;
import com.karate.payment_service.domain.model.UserAccountEntity;
import com.karate.payment_service.domain.repository.PaymentItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UnpaidCalculator {

    private final PaymentItemRepository items;

    public List<YearMonth> unpaidMonths(UserAccountEntity user) {
        var reg = YearMonth.from(user.getRegistrationDate());
        var now = YearMonth.now();

        var paid = items.findByUserIdAndStatus(user.getUserId(), PaymentStatus.PAID)
                .stream().map(PaymentItemEntity::getYearMonth).collect(Collectors.toSet());

        List<YearMonth> result = new ArrayList<>();
        for (YearMonth ym = reg; !ym.isAfter(now); ym = ym.plusMonths(1)) {
            if (!paid.contains(ym)) result.add(ym);
        }
        return result;
    }
}
