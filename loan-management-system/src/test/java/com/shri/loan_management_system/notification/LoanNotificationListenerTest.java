package com.shri.loan_management_system.notification;

import com.shri.loan_management_system.loan.LoanStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LoanNotificationListenerTest {

    @Mock EmailService emailService;
    @InjectMocks LoanNotificationListener listener;

    @Test
    void approvalEmailContainsTheAgreedTerms() {
        LoanDecisionEvent event = new LoanDecisionEvent("asha@mail.com", "Asha Rao",
                "LN100", "Personal Loan", new BigDecimal("200000"), 24, LoanStatus.APPROVED,
                new BigDecimal("12.00"), new BigDecimal("9414.69"), null);

        listener.onLoanDecision(event);

        ArgumentCaptor<String> subject = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(emailService).send(eq("asha@mail.com"), subject.capture(), body.capture());
        assertThat(subject.getValue()).contains("LN100").contains("approved");
        assertThat(body.getValue()).contains("Asha Rao", "200000", "24 months", "12.00%", "9414.69");
    }

    @Test
    void rejectionEmailContainsTheReason() {
        LoanDecisionEvent event = new LoanDecisionEvent("asha@mail.com", "Asha Rao",
                "LN101", "Personal Loan", new BigDecimal("100000"), 12, LoanStatus.REJECTED,
                null, null, "Income documents missing");

        listener.onLoanDecision(event);

        ArgumentCaptor<String> subject = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(emailService).send(eq("asha@mail.com"), subject.capture(), body.capture());
        assertThat(subject.getValue()).contains("LN101").doesNotContain("approved");
        assertThat(body.getValue()).contains("could not be approved", "Income documents missing");
    }
}