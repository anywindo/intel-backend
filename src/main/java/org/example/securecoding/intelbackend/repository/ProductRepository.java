package org.example.securecoding.intelbackend.repository;

import org.example.securecoding.intelbackend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    // Find products by category (e.g., "Processors", "Chipsets")
    List<Product> findByCategory(String category);

    // Find products by status (e.g., "Launched", "Announced")
    List<Product> findByStatus(String status);

    // Find root-level products (no parent)
    List<Product> findByParentIsNull();
}
