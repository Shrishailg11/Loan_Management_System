package com.shri.loan_management_system.product;

import com.shri.loan_management_system.product.dto.ProductRequest;
import com.shri.loan_management_system.product.dto.ProductResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class LoanProductController {

    private final LoanProductService service;

    @GetMapping
    public List<ProductResponse> listActive() {
        return service.listActive();
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProductResponse> listAll() {
        return service.listAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse setActive(@PathVariable Long id, @RequestParam boolean value) {
        return service.setActive(id, value);
    }
}