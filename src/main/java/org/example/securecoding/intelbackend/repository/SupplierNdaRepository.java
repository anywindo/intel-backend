package org.example.securecoding.intelbackend.repository;

import org.example.securecoding.intelbackend.model.SupplierNda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierNdaRepository extends JpaRepository<SupplierNda, String> {

    // Find all NDAs for a specific supplier
    List<SupplierNda> findBySupplierId(String supplierId);
}
