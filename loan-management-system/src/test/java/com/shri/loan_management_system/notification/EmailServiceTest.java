package com.shri.loan_management_system.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock JavaMailSender mailSender;

    @Test
    void sendsMessageWithSenderRecipientSubjectAndBody() {
        EmailService service = new EmailService(mailSender, "noreply@bank.local", true);

        service.send("asha@mail.com", "Hello", "Body text");

        ArgumentCaptor<SimpleMailMessage> sent = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(sent.capture());
        assertThat(sent.getValue().getFrom()).isEqualTo("noreply@bank.local");
        assertThat(sent.getValue().getTo()).containsExactly("asha@mail.com");
        assertThat(sent.getValue().getSubject()).isEqualTo("Hello");
        assertThat(sent.getValue().getText()).isEqualTo("Body text");
    }

    @Test
    void mailFailureIsSwallowedSoCallersAreNotAffected() {
        doThrow(new MailSendException("server down"))
                .when(mailSender).send(any(SimpleMailMessage.class));
        EmailService service = new EmailService(mailSender, "noreply@bank.local", true);

        assertThatCode(() -> service.send("asha@mail.com", "Hello", "Body"))
                .doesNotThrowAnyException();
    }

    @Test
    void doesNothingWhenDisabled() {
        EmailService service = new EmailService(mailSender, "noreply@bank.local", false);

        service.send("asha@mail.com", "Hello", "Body");

        verifyNoInteractions(mailSender);
    }
}