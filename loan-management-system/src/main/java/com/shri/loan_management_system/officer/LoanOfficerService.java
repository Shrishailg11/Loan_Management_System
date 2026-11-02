package com.shri.loan_management_system.officer;

import com.shri.loan_management_system.common.exception.DuplicateResourceException;
import com.shri.loan_management_system.officer.dto.CreateOfficerRequest;
import com.shri.loan_management_system.officer.dto.OfficerResponse;
import com.shri.loan_management_system.user.Role;
import com.shri.loan_management_system.user.User;
import com.shri.loan_management_system.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoanOfficerService {

    private final UserRepository userRepository;
    private final LoanOfficerRepository loanOfficerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public OfficerResponse createOfficer(CreateOfficerRequest request) {
        String email = request.email().trim().toLowerCase();
        String employeeId = request.employeeId().trim().toUpperCase();

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email is already registered");
        }
        if (loanOfficerRepository.existsByEmployeeId(employeeId)) {
            throw new DuplicateResourceException("Employee id is already in use");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());
        user.setRole(Role.LOAN_OFFICER);
        userRepository.save(user);

        LoanOfficer officer = new LoanOfficer();
        officer.setUser(user);
        officer.setEmployeeId(employeeId);
        officer.setApprovalLimit(request.approvalLimit());
        loanOfficerRepository.save(officer);

        return new OfficerResponse(user.getId(), officer.getId(), user.getName(),
                user.getEmail(), officer.getEmployeeId(), officer.getApprovalLimit());
    }
}