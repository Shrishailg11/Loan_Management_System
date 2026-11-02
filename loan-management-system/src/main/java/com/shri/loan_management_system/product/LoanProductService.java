package com.shri.loan_management_system.product;

import com.shri.loan_management_system.common.exception.BusinessRuleException;
import com.shri.loan_management_system.common.exception.DuplicateResourceException;
import com.shri.loan_management_system.common.exception.ResourceNotFoundException;
import com.shri.loan_management_system.product.dto.ProductRequest;
import com.shri.loan_management_system.product.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanProductService {

    private final LoanProductRepository repository;

    @Transactional
    public ProductResponse create(ProductRequest request) {
        String name = request.name().trim();
        if (repository.existsByName(name)) {
            throw new DuplicateResourceException("A product named '" + name + "' already exists");
        }
        validateRanges(request);

        LoanProduct product = new LoanProduct();
        apply(product, request, name);
        return ProductResponse.from(repository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        LoanProduct product = find(id);
        String name = request.name().trim();
        if (repository.existsByNameAndIdNot(name, id)) {
            throw new DuplicateResourceException("A product named '" + name + "' already exists");
        }
        validateRanges(request);

        apply(product, request, name);
        return ProductResponse.from(product);   // no save() needed
    }

    @Transactional
    public ProductResponse setActive(Long id, boolean active) {
        LoanProduct product = find(id);
        product.setActive(active);
        return ProductResponse.from(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listActive() {
        return repository.findByActiveTrue().stream().map(ProductResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listAll() {
        return repository.findAll().stream().map(ProductResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return ProductResponse.from(find(id));
    }

    private LoanProduct find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan product", id));
    }

    private void validateRanges(ProductRequest r) {
        if (r.maxAmount().compareTo(r.minAmount()) < 0) {
            throw new BusinessRuleException("Maximum amount cannot be less than minimum amount");
        }
        if (r.maxTenureMonths() < r.minTenureMonths()) {
            throw new BusinessRuleException("Maximum tenure cannot be less than minimum tenure");
        }
    }

    private void apply(LoanProduct product, ProductRequest r, String name) {
        product.setName(name);
        product.setInterestRate(r.interestRate());
        product.setMinAmount(r.minAmount());
        product.setMaxAmount(r.maxAmount());
        product.setMinTenureMonths(r.minTenureMonths());
        product.setMaxTenureMonths(r.maxTenureMonths());
    }
}