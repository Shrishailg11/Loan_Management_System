package com.shri.loan_management_system.loan;

import com.shri.loan_management_system.common.exception.BusinessRuleException;
import com.shri.loan_management_system.common.exception.ResourceNotFoundException;
import com.shri.loan_management_system.customer.Customer;
import com.shri.loan_management_system.customer.CustomerRepository;
import com.shri.loan_management_system.loan.dto.LoanApplyRequest;
import com.shri.loan_management_system.loan.dto.LoanResponse;
import com.shri.loan_management_system.product.LoanProduct;
import com.shri.loan_management_system.product.LoanProductRepository;
import com.shri.loan_management_system.schedule.EmiCalculator;
import com.shri.loan_management_system.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private static final BigDecimal MAX_EMI_TO_INCOME_RATIO = new BigDecimal("0.50");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final LoanRepository loanRepository;
    private final LoanProductRepository productRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public LoanResponse apply(Long userId, LoanApplyRequest request) {
        Customer customer = findCustomer(userId);

        LoanProduct product = productRepository.findById(request.productId())
                .filter(LoanProduct::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Loan product", request.productId()));

        validateAgainstProduct(product, request);
        checkAffordability(customer, product, request);

        Loan loan = new Loan();
        loan.setLoanNo(generateLoanNo());
        loan.setCustomer(customer);
        loan.setProduct(product);
        loan.setPrincipal(request.amount());
        loan.setTenureMonths(request.tenureMonths());
        // status defaults to PENDING; interestRate, emiAmount and approvedBy stay null until approval

        return LoanResponse.from(loanRepository.save(loan));
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> myLoans(Long userId) {
        Customer customer = findCustomer(userId);
        return loanRepository.findByCustomerIdWithProduct(customer.getId())
                .stream().map(LoanResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public LoanResponse getLoan(Long loanId, Long userId, Role role) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", loanId));

        boolean isStaff = role == Role.LOAN_OFFICER || role == Role.ADMIN;
        boolean isOwner = loan.getCustomer().getUser().getId().equals(userId);

        if (!isStaff && !isOwner) {
            // 404 rather than 403, so we do not reveal that this loan id exists
            throw new ResourceNotFoundException("Loan", loanId);
        }
        return LoanResponse.from(loan);
    }

    private Customer findCustomer(Long userId) {
        return customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for this user"));
    }

    private void validateAgainstProduct(LoanProduct product, LoanApplyRequest request) {
        BigDecimal amount = request.amount();
        if (amount.compareTo(product.getMinAmount()) < 0 || amount.compareTo(product.getMaxAmount()) > 0) {
            throw new BusinessRuleException("Amount for " + product.getName() + " must be between "
                    + product.getMinAmount() + " and " + product.getMaxAmount());
        }
        int tenure = request.tenureMonths();
        if (tenure < product.getMinTenureMonths() || tenure > product.getMaxTenureMonths()) {
            throw new BusinessRuleException("Tenure for " + product.getName() + " must be between "
                    + product.getMinTenureMonths() + " and " + product.getMaxTenureMonths() + " months");
        }
    }

    private void checkAffordability(Customer customer, LoanProduct product, LoanApplyRequest request) {
        BigDecimal estimatedEmi = EmiCalculator.calculateEmi(
                request.amount(), product.getInterestRate(), request.tenureMonths());
        BigDecimal maxAllowedEmi = customer.getMonthlyIncome().multiply(MAX_EMI_TO_INCOME_RATIO);

        if (estimatedEmi.compareTo(maxAllowedEmi) > 0) {
            throw new BusinessRuleException("Estimated EMI of " + estimatedEmi
                    + " exceeds 50% of your monthly income");
        }
    }

    private String generateLoanNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        for (int attempt = 0; attempt < 5; attempt++) {
            String candidate = "LN" + datePart + String.format("%06d", RANDOM.nextInt(1_000_000));
            if (!loanRepository.existsByLoanNo(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Could not generate a unique loan number");
    }
}