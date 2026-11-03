package com.shri.loan_management_system.transaction;

import com.shri.loan_management_system.transaction.dto.PaymentRequest;
import com.shri.loan_management_system.transaction.dto.PaymentResponse;
import com.shri.loan_management_system.transaction.dto.TransactionResponse;
import com.shri.loan_management_system.user.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans/{loanId}")
@RequiredArgsConstructor
public class LoanPaymentController {

    private final LoanPaymentService paymentService;

    @PostMapping("/payments")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentResponse> pay(@PathVariable Long loanId,
                                               @AuthenticationPrincipal Jwt jwt,
                                               @Valid @RequestBody PaymentRequest request) {
        Long userId = jwt.getClaim("userId");
        PaymentResponse response = paymentService.pay(loanId, userId, request.referenceNo());
        HttpStatus status = response.replayed() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/transactions")
    public List<TransactionResponse> history(@PathVariable Long loanId,
                                             @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        Role role = Role.valueOf(jwt.getClaimAsString("role"));
        return paymentService.history(loanId, userId, role);
    }
}