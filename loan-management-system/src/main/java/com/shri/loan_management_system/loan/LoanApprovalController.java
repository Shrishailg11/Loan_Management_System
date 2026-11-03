package com.shri.loan_management_system.loan;

import com.shri.loan_management_system.loan.dto.LoanResponse;
import com.shri.loan_management_system.loan.dto.RejectRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/officer/loans")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LOAN_OFFICER')")
public class LoanApprovalController {

    private final LoanApprovalService approvalService;

    @GetMapping("/pending")
    public List<LoanResponse> pending() {
        return approvalService.listPending();
    }

    @PostMapping("/{id}/approve")
    public LoanResponse approve(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        return approvalService.approve(id, userId);
    }

    @PostMapping("/{id}/reject")
    public LoanResponse reject(@PathVariable Long id,
                               @Valid @RequestBody RejectRequest request,
                               @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        return approvalService.reject(id, userId, request.reason());
    }
}