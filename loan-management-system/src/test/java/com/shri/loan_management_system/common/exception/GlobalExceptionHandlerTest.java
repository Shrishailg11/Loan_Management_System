package com.shri.loan_management_system.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request =
            new MockHttpServletRequest("GET", "/api/loans/99");

    @Test
    void notFoundReturns404WithMessageAndPath() {
        var response = handler.handleNotFound(new ResourceNotFoundException("Loan", 99), request);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody().message()).isEqualTo("Loan not found with id: 99");
        assertThat(response.getBody().path()).isEqualTo("/api/loans/99");
    }

    @Test
    void duplicateReturns409() {
        var response = handler.handleDuplicate(
                new DuplicateResourceException("Email already registered"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void businessRuleReturns400() {
        var response = handler.handleBadRequest(
                new BusinessRuleException("Loan is already rejected"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void unexpectedErrorHidesInternalDetails() {
        var response = handler.handleUnexpected(new RuntimeException("db password leaked"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().message()).doesNotContain("db password");
    }
}