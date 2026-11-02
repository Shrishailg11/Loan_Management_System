package com.shri.loan_management_system.officer;

import com.shri.loan_management_system.common.exception.DuplicateResourceException;
import com.shri.loan_management_system.officer.dto.CreateOfficerRequest;
import com.shri.loan_management_system.officer.dto.OfficerResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanOfficerServiceTest {

    @Mock UserRepository userRepository;
    @Mock LoanOfficerRepository loanOfficerRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks LoanOfficerService officerService;

    private CreateOfficerRequest request() {
        return new CreateOfficerRequest("Ravi Kumar", "Ravi@Bank.com", "Officer@123",
                "9123456780", "emp-101", new BigDecimal("1000000"));
    }

    @Test
    void createsOfficerWithOfficerRoleAndHashedPassword() {
        when(passwordEncoder.encode("Officer@123")).thenReturn("HASHED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(loanOfficerRepository.save(any(LoanOfficer.class))).thenAnswer(inv -> inv.getArgument(0));

        OfficerResponse response = officerService.createOfficer(request());

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getRole()).isEqualTo(Role.LOAN_OFFICER);
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("HASHED");
        assertThat(response.employeeId()).isEqualTo("EMP-101");
    }

    @Test
    void rejectsDuplicateEmail() {
        when(userRepository.existsByEmail("ravi@bank.com")).thenReturn(true);

        assertThatThrownBy(() -> officerService.createOfficer(request()))
                .isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void rejectsDuplicateEmployeeId() {
        when(loanOfficerRepository.existsByEmployeeId("EMP-101")).thenReturn(true);

        assertThatThrownBy(() -> officerService.createOfficer(request()))
                .isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any());
    }
}