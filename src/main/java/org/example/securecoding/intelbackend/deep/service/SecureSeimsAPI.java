package org.example.securecoding.intelbackend.deep.service;

import org.example.securecoding.intelbackend.deep.domain.SecureSession;
import org.example.securecoding.intelbackend.model.Employee;
import org.example.securecoding.intelbackend.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * DEEP MODEL — Secure Logic: SEIMS (Secure Employee Info Management System)
 *
 * This service demonstrates high-assurance security by requiring 
 * a SecureSession domain primitive. 
 *
 * VULNERABILITY MITIGATION:
 * This service cannot be abused by IDOR or Session Hijacking because
 * the constructor of SecureSession enforces that the token, user, and IP 
 * are all bound correctly.
 */
@Service
public class SecureSeimsAPI {

    private final EmployeeRepository repository;

    public SecureSeimsAPI(EmployeeRepository repository) {
        this.repository = repository;
    }

    public List<Employee> getSensitiveData(SecureSession session) {
        // The service logic is protected by the 'SecureSession' type invariant.
        // If we have an instance of SecureSession, we KNOW it is authorized.
        return repository.findAll();
    }
}
