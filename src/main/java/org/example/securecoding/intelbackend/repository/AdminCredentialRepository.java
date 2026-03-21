package org.example.securecoding.intelbackend.repository;

import org.example.securecoding.intelbackend.model.AdminCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminCredentialRepository extends JpaRepository<AdminCredential, Long> {

    // Find admin credential by username (for login)
    Optional<AdminCredential> findByUsername(String username);
}
