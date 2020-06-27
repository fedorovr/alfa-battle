package com.github.fedorovr.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class StatsInfo {
    String oftenCategoryId;
    String rareCategoryId;
    String maxAmountCategoryId;
    String minAmountCategoryId;
}
