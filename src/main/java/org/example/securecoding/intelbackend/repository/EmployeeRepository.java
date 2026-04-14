package org.example.securecoding.intelbackend.repository;

import org.example.securecoding.intelbackend.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {

    // Custom query to find a specific employee by name (for normal, safe operation)
    List<Employee> findByFullNameContainingIgnoreCase(String name);
}
