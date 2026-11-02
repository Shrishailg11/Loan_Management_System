package com.shri.loan_management_system.auth;

import com.shri.loan_management_system.auth.dto.LoginRequest;
import com.shri.loan_management_system.auth.dto.LoginResponse;
import com.shri.loan_management_system.auth.dto.RegisterRequest;
import com.shri.loan_management_system.auth.dto.RegisterResponse;
import com.shri.loan_management_system.common.exception.BusinessRuleException;
import com.shri.loan_management_system.common.exception.DuplicateResourceException;
import com.shri.loan_management_system.customer.Customer;
import com.shri.loan_management_system.customer.CustomerRepository;
import com.shri.loan_management_system.user.Role;
import com.shri.loan_management_system.user.User;
import com.shri.loan_management_system.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MINIMUM_AGE = 18;

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    @Transactional
    public RegisterResponse registerCustomer(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        String pan = request.panNumber().trim().toUpperCase();

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email is already registered");
        }
        if (customerRepository.existsByPanNumber(pan)) {
            throw new DuplicateResourceException("PAN is already registered");
        }
        if (Period.between(request.dateOfBirth(), LocalDate.now()).getYears() < MINIMUM_AGE) {
            throw new BusinessRuleException("You must be at least 18 years old to register");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());
        user.setRole(Role.CUSTOMER);
        userRepository.save(user);

        Customer customer = new Customer();
        customer.setUser(user);
        customer.setPanNumber(pan);
        customer.setMonthlyIncome(request.monthlyIncome());
        customer.setDateOfBirth(request.dateOfBirth());
        customerRepository.save(customer);

        return new RegisterResponse(user.getId(), customer.getId(),
                user.getName(), user.getEmail(), user.getRole());
    }

    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        boolean passwordMatches = passwordEncoder.matches(request.password(), user.getPasswordHash());
        if (!passwordMatches || !user.isEnabled()) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);
        return new LoginResponse(token, "Bearer", jwtService.getExpirationSeconds(), user.getRole());
    }
}