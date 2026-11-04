package com.shri.loan_management_system.schedule;

import com.shri.loan_management_system.schedule.dto.OverdueInstallmentResponse;
import com.shri.loan_management_system.schedule.dto.OverdueSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/overdue")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('LOAN_OFFICER', 'ADMIN')")
public class OverdueController {

    private final OverdueService overdueService;

    @GetMapping
    public List<OverdueInstallmentResponse> list() {
        return overdueService.listOverdue();
    }

    @GetMapping("/summary")
    public OverdueSummary summary() {
        return overdueService.summarize();
    }
}