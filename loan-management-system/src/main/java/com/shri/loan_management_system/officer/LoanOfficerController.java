package com.shri.loan_management_system.officer;

import com.shri.loan_management_system.officer.dto.CreateOfficerRequest;
import com.shri.loan_management_system.officer.dto.OfficerResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/officers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class LoanOfficerController {

    private final LoanOfficerService officerService;

    @PostMapping
    public ResponseEntity<OfficerResponse> create(@Valid @RequestBody CreateOfficerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(officerService.createOfficer(request));
    }
}