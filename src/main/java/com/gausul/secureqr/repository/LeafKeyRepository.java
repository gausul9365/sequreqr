package com.gausul.secureqr.repository;


import com.gausul.secureqr.model.LeafKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeafKeyRepository extends JpaRepository<LeafKey, String> {
    Optional<LeafKey> findByAlias(String alias);
}
