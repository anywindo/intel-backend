package org.example.securecoding.intelbackend.repository;

import org.example.securecoding.intelbackend.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, String> {

    // Search suppliers by company name
    List<Supplier> findByCompanyNameContainingIgnoreCase(String name);

    // Filter suppliers by country
    List<Supplier> findByCountry(String country);
}
