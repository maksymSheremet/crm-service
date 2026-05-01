package my.code.crmservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

// @EnableScheduling активує Spring scheduler.
// БЕЗ цього @Scheduled методи не викликатимуться взагалі.
// Окремий клас (не в CrmServiceApplication) — легше вимкнути в тестах
@Configuration
@EnableScheduling
public class SchedulingConfig {
}