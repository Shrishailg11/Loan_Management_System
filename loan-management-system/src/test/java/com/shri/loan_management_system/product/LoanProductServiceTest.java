package com.shri.loan_management_system.product;

import com.shri.loan_management_system.common.exception.BusinessRuleException;
import com.shri.loan_management_system.common.exception.DuplicateResourceException;
import com.shri.loan_management_system.common.exception.ResourceNotFoundException;
import com.shri.loan_management_system.product.dto.ProductRequest;
import com.shri.loan_management_system.product.dto.ProductResponse;
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
class LoanProductServiceTest {

    @Mock LoanProductRepository repository;
    @InjectMocks LoanProductService service;

    private ProductRequest request(String minAmount, String maxAmount, int minTenure, int maxTenure) {
        return new ProductRequest("Home Loan", new BigDecimal("8.50"),
                new BigDecimal(minAmount), new BigDecimal(maxAmount), minTenure, maxTenure);
    }

    @Test
    void createsActiveProduct() {
        when(repository.save(any(LoanProduct.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponse response = service.create(request("500000", "5000000", 60, 360));

        assertThat(response.name()).isEqualTo("Home Loan");
        assertThat(response.active()).isTrue();
    }

    @Test
    void rejectsDuplicateName() {
        when(repository.existsByName("Home Loan")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("500000", "5000000", 60, 360)))
                .isInstanceOf(DuplicateResourceException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsMaxAmountBelowMinAmount() {
        assertThatThrownBy(() -> service.create(request("500000", "100000", 60, 360)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void rejectsMaxTenureBelowMinTenure() {
        assertThatThrownBy(() -> service.create(request("500000", "5000000", 360, 60)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void updateFailsWhenProductDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, request("500000", "5000000", 60, 360)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deactivateFlipsActiveFlag() {
        LoanProduct product = new LoanProduct();
        product.setName("Home Loan");
        when(repository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = service.setActive(1L, false);

        assertThat(response.active()).isFalse();
    }
}