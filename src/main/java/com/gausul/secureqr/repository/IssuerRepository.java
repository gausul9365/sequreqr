package com.gausul.secureqr.repository;

import com.gausul.secureqr.model.Issuer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IssuerRepository extends JpaRepository<Issuer, String> {
    Optional<Issuer> findFirstByOrderByCreatedAtAsc();
    // findById built-in
}
