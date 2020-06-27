package com.github.fedorovr.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserInfo {
    String userId;
    BigDecimal totalSum;
    Map<String, CategoryInfo> analyticInfo;
}
