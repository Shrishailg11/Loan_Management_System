package com.shri.loan_management_system.loan;

import com.shri.loan_management_system.common.exception.BusinessRuleException;
import com.shri.loan_management_system.common.exception.ResourceNotFoundException;
import com.shri.loan_management_system.customer.Customer;
import com.shri.loan_management_system.customer.CustomerRepository;
import com.shri.loan_management_system.loan.dto.LoanApplyRequest;
import com.shri.loan_management_system.loan.dto.LoanResponse;
import com.shri.loan_management_system.product.LoanProduct;
import com.shri.loan_management_system.product.LoanProductRepository;
import com.shri.loan_management_system.user.Role;
import com.shri.loan_management_system.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock LoanRepository loanRepository;
    @Mock LoanProductRepository productRepository;
    @Mock CustomerRepository customerRepository;
    @InjectMocks LoanService loanService;

    private Customer customer;
    private LoanProduct product;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);

        customer = new Customer();
        customer.setId(10L);
        customer.setUser(user);
        customer.setMonthlyIncome(new BigDecimal("60000"));

        product = new LoanProduct();
        product.setId(2L);
        product.setName("Personal Loan");
        product.setInterestRate(new BigDecimal("12.00"));
        product.setMinAmount(new BigDecimal("50000"));
        product.setMaxAmount(new BigDecimal("1000000"));
        product.setMinTenureMonths(12);
        product.setMaxTenureMonths(60);
        product.setActive(true);
    }

    private LoanApplyRequest apply(String amount, int tenure) {
        return new LoanApplyRequest(2L, new BigDecimal(amount), tenure);
    }

    private void givenCustomerAndProduct() {
        when(customerRepository.findByUserId(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
    }

    @Test
    void createsPendingLoanWithoutRateOrEmi() {
        givenCustomerAndProduct();
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        LoanResponse response = loanService.apply(1L, apply("200000", 24));

        assertThat(response.status()).isEqualTo(LoanStatus.PENDING);
        assertThat(response.loanNo()).startsWith("LN");
        assertThat(response.interestRate()).isNull();
        assertThat(response.emiAmount()).isNull();
    }

    @Test
    void rejectsInactiveProduct() {
        product.setActive(false);
        givenCustomerAndProduct();

        assertThatThrownBy(() -> loanService.apply(1L, apply("200000", 24)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(loanRepository, never()).save(any());
    }

    @Test
    void rejectsAmountOutsideProductLimits() {
        givenCustomerAndProduct();

        assertThatThrownBy(() -> loanService.apply(1L, apply("10000", 24)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void rejectsTenureOutsideProductLimits() {
        givenCustomerAndProduct();

        assertThatThrownBy(() -> loanService.apply(1L, apply("200000", 6)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void rejectsWhenEmiExceedsHalfOfIncome() {
        givenCustomerAndProduct();

        assertThatThrownBy(() -> loanService.apply(1L, apply("1000000", 12)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("50%");
        verify(loanRepository, never()).save(any());
    }

    @Test
    void failsWhenCustomerProfileMissing() {
        when(customerRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.apply(1L, apply("200000", 24)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Loan existingLoan() {
        Loan loan = new Loan();
        loan.setId(5L);
        loan.setCustomer(customer);
        loan.setProduct(product);
        loan.setPrincipal(new BigDecimal("200000"));
        loan.setTenureMonths(24);
        return loan;
    }

    @Test
    void ownerCanViewOwnLoan() {
        when(loanRepository.findById(5L)).thenReturn(Optional.of(existingLoan()));

        LoanResponse response = loanService.getLoan(5L, 1L, Role.CUSTOMER);

        assertThat(response.id()).isEqualTo(5L);
    }

    @Test
    void otherCustomerCannotViewLoan() {
        when(loanRepository.findById(5L)).thenReturn(Optional.of(existingLoan()));

        assertThatThrownBy(() -> loanService.getLoan(5L, 2L, Role.CUSTOMER))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void officerCanViewAnyLoan() {
        when(loanRepository.findById(5L)).thenReturn(Optional.of(existingLoan()));

        LoanResponse response = loanService.getLoan(5L, 99L, Role.LOAN_OFFICER);

        assertThat(response.id()).isEqualTo(5L);
    }
}