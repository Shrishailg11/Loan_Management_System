package com.shri.loan_management_system.schedule;

import com.shri.loan_management_system.schedule.dto.InstallmentResponse;
import com.shri.loan_management_system.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/loans/{loanId}/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    public List<InstallmentResponse> get(@PathVariable Long loanId,
                                         @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        Role role = Role.valueOf(jwt.getClaimAsString("role"));
        return scheduleService.getSchedule(loanId, userId, role);
    }
}