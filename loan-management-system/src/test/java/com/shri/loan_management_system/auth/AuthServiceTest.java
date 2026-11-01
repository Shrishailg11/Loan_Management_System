package com.shri.loan_management_system.auth;

import com.shri.loan_management_system.auth.dto.RegisterRequest;
import com.shri.loan_management_system.auth.dto.RegisterResponse;
import com.shri.loan_management_system.common.exception.BusinessRuleException;
import com.shri.loan_management_system.common.exception.DuplicateResourceException;
import com.shri.loan_management_system.customer.Customer;
import com.shri.loan_management_system.customer.CustomerRepository;
import com.shri.loan_management_system.user.Role;
import com.shri.loan_management_system.user.User;
import com.shri.loan_management_system.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock CustomerRepository customerRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks AuthService authService;

    private RegisterRequest request(LocalDate dob) {
        return new RegisterRequest("Asha Rao", "Asha@Mail.com", "Secret@123",
                "9876543210", "abcde1234f", new BigDecimal("60000"), dob);
    }

    @Test
    void registersCustomerWithHashedPasswordAndNormalizedEmail() {
        when(passwordEncoder.encode("Secret@123")).thenReturn("HASHED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        RegisterResponse response = authService.registerCustomer(request(LocalDate.of(1995, 5, 10)));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("HASHED");
        assertThat(saved.getValue().getEmail()).isEqualTo("asha@mail.com");
        assertThat(response.role()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    void rejectsDuplicateEmail() {
        when(userRepository.existsByEmail("asha@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerCustomer(request(LocalDate.of(1995, 5, 10))))
                .isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void rejectsUnderageApplicant() {
        assertThatThrownBy(() -> authService.registerCustomer(request(LocalDate.now().minusYears(17))))
                .isInstanceOf(BusinessRuleException.class);
        verify(userRepository, never()).save(any());
    }
}