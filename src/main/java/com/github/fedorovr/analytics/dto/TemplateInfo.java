package com.github.fedorovr.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TemplateInfo {
    String recipientId;
    String categoryId;
    String amount;
}
