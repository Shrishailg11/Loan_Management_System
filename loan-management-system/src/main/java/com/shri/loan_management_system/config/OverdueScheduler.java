package com.shri.loan_management_system.config;

import com.shri.loan_management_system.schedule.OverdueService;
import com.shri.loan_management_system.schedule.dto.OverdueSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OverdueScheduler {

    private final OverdueService overdueService;

    @Scheduled(cron = "${app.overdue.cron:0 0 9 * * *}")
    public void logOverdueSummary() {
        OverdueSummary summary = overdueService.summarize();

        if (summary.overdueInstallments() == 0) {
            log.info("Overdue check: no overdue installments");
            return;
        }
        log.warn("Overdue check: {} installment(s) across {} loan(s), total {} overdue",
                summary.overdueInstallments(), summary.affectedLoans(), summary.totalOverdueAmount());
        // future: send reminder emails here
    }
}