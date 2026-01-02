package com.karate.payment_service.infrastructure.jpa;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

class YearMonthAttributeConverterTest {

    YearMonthAttributeConverter c = new YearMonthAttributeConverter();

    @Test
    void convertToDatabaseColumn_null() {
        assertThat(c.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    void convertToDatabaseColumn_value() {
        assertThat(c.convertToDatabaseColumn(YearMonth.of(2025, 1))).isEqualTo("2025-01");
    }

    @Test
    void convertToEntityAttribute_null() {
        assertThat(c.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void convertToEntityAttribute_value() {
        assertThat(c.convertToEntityAttribute("2025-12")).isEqualTo(YearMonth.of(2025, 12));
    }
}
