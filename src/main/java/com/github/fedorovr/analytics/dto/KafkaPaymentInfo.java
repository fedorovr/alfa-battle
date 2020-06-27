package com.github.fedorovr.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KafkaPaymentInfo {
    String ref;
    String categoryId;
    String userId;
    String recipientId;
    String desc;
    BigDecimal amount;
}
