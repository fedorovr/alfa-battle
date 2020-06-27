package com.github.fedorovr.analytics;

import com.github.fedorovr.analytics.dto.*;
import com.github.fedorovr.analytics.mvc.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class Controller {
    private final KafkaConsumer kafkaConsumer;

    @Autowired
    public Controller(KafkaConsumer kafkaConsumer) {
        this.kafkaConsumer = kafkaConsumer;
    }

    @RequestMapping("/admin/health")
    @ResponseBody
    String healthCheck() {
        return "{\"status\":\"UP\"}";
    }

    @RequestMapping("/analytic")
    @ResponseBody
    List<UserInfo> getAnalytic() {
        Map<String, List<KafkaPaymentInfo>> paymentsByUser = kafkaConsumer.getPaymentsByUser();
        Map<String, Map<String, List<KafkaPaymentInfo>>> paymentsByUserByCategory = new HashMap<>();
        Map<String, Map<String, CategoryInfo>> paymentsByUserByCategoryProcessed = new HashMap<>();
        for (Map.Entry<String, List<KafkaPaymentInfo>> payment : paymentsByUser.entrySet()) {
            paymentsByUserByCategory.putIfAbsent(payment.getKey(), new HashMap<>());
            Map<String, List<KafkaPaymentInfo>> userPayments = paymentsByUserByCategory.get(payment.getKey());
            for (KafkaPaymentInfo info : payment.getValue()) {
                userPayments.putIfAbsent(info.getCategoryId(), new ArrayList<>());
                userPayments.get(info.getCategoryId()).add(info);
            }
        }

        for (Map.Entry<String, Map<String, List<KafkaPaymentInfo>>> userPayments : paymentsByUserByCategory.entrySet()) {
            paymentsByUserByCategoryProcessed.putIfAbsent(userPayments.getKey(), new HashMap<>());
            Map<String, CategoryInfo> userPaymentsByCategoryProcessed = paymentsByUserByCategoryProcessed.get(userPayments.getKey());
            for (Map.Entry<String, List<KafkaPaymentInfo>> categoryPayments : userPayments.getValue().entrySet()) {
                CategoryInfo categoryInfo = CategoryInfo.fromList(categoryPayments.getValue());
                userPaymentsByCategoryProcessed.putIfAbsent(categoryPayments.getKey(), categoryInfo);
            }
        }

        return paymentsByUserByCategoryProcessed.entrySet().stream().map(
                userPayments ->
                        new UserInfo(userPayments.getKey(),
                                userPayments.getValue().values().stream().map(CategoryInfo::getSum).reduce(BigDecimal.ZERO, BigDecimal::add),
                                userPayments.getValue())
        ).collect(Collectors.toList());
    }

    @RequestMapping("/analytic/{userId}")
    @ResponseBody
    UserInfo getAnalyticByUser(@PathVariable String userId) {
        return getAnalytic().stream().filter(info -> info.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(UserNotFoundException::new);
    }

    @RequestMapping("/analytic/{userId}/stats")
    @ResponseBody
    StatsInfo getAnalyticStats(@PathVariable String userId) {
        Map<String, List<KafkaPaymentInfo>> paymentsByUser = kafkaConsumer.getPaymentsByUser();
        List<KafkaPaymentInfo> payments = paymentsByUser.get(userId);
        if (payments == null) {
            throw new UserNotFoundException();
        }
        Map<String, Integer> frequencyByCategory = new HashMap<>();
        Map<String, BigDecimal> amountsByCategory = new HashMap<>();
        for (KafkaPaymentInfo payment : payments) {
            String categoryId = payment.getCategoryId();
            frequencyByCategory.merge(categoryId, 1, Integer::sum);
            amountsByCategory.merge(categoryId, payment.getAmount(), BigDecimal::add);
        }
        return new StatsInfo(
                frequencyByCategory.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null),
                frequencyByCategory.entrySet().stream().min(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null),
                amountsByCategory.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null),
                amountsByCategory.entrySet().stream().min(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null)
        );
    }

    @RequestMapping("/analytic/{userId}/templates")
    @ResponseBody
    List<TemplateInfo> getAnalyticTemplates(@PathVariable String userId) {
        Map<String, List<KafkaPaymentInfo>> paymentsByUser = kafkaConsumer.getPaymentsByUser();
        List<KafkaPaymentInfo> payments = paymentsByUser.get(userId);
        if (payments == null) {
            throw new UserNotFoundException();
        }
        Map<TemplateInfo, Integer> templatesWithCount = new HashMap<>();
        for (KafkaPaymentInfo payment : payments) {
            TemplateInfo templateInfo = new TemplateInfo(payment.getRecipientId(), payment.getCategoryId(),
                    payment.getAmount().setScale(8, RoundingMode.HALF_UP).toPlainString());
            templatesWithCount.merge(templateInfo, 1, Integer::sum);
        }
        return templatesWithCount.entrySet().stream().filter(e -> e.getValue() >= 3)
                .map(Map.Entry::getKey).collect(Collectors.toList());
    }
}
