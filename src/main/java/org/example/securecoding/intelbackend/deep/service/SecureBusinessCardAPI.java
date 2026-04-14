package org.example.securecoding.intelbackend.deep.service;

import org.example.securecoding.intelbackend.deep.domain.EmployeeSearchQuery;
import org.example.securecoding.intelbackend.deep.domain.VerifiedSession;
import org.example.securecoding.intelbackend.model.Employee;
import org.example.securecoding.intelbackend.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * DEEP MODEL — Secure Logic: Business Card Portal
 *
 * This service enforces the 'Secure by Design' principles by requiring
 * domain primitives (VerifiedSession, EmployeeSearchQuery) as inputs.
 *
 * This makes it impossible to call the search without a verified session
 * or with an invalid (empty/too short) query.
 */
@Service
public class SecureBusinessCardAPI {

    private final EmployeeRepository repository;

    public SecureBusinessCardAPI(EmployeeRepository repository) {
        this.repository = repository;
    }

    public List<Employee> searchEmployees(VerifiedSession session, EmployeeSearchQuery query) {
        // At this point, session is ALREADY verified in its constructor.
        // If it weren't valid, the object wouldn't exist.
        
        // Similarly, query is ALREADY validated.
        
        // VULNERABILITY MITIGATION: We only call the specific filter search,
        // never repository.findAll(), preventing mass data leaks even if an 
        // attacker bypassed the presentation layer.
        return repository.findByFullNameContainingIgnoreCase(query.getValue());
    }
}
