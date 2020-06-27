package com.github.fedorovr.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CategoryInfo {
    BigDecimal min;
    BigDecimal max;
    BigDecimal sum;

    public static CategoryInfo fromList(List<KafkaPaymentInfo> payments) {
        if (payments.isEmpty()) {
            throw new IllegalArgumentException("payments list is empty");
        }
        BigDecimal min = payments.get(0).getAmount();
        BigDecimal max = payments.get(0).getAmount();
        BigDecimal sum = BigDecimal.ZERO;

        for (KafkaPaymentInfo payment : payments) {
            if (payment.getAmount().compareTo(min) < 0) {
                min = payment.getAmount();
            }
            if (payment.getAmount().compareTo(max) > 0) {
                max = payment.getAmount();
            }
            sum = sum.add(payment.getAmount());
        }
        return new CategoryInfo(min, max, sum);
    }
}
