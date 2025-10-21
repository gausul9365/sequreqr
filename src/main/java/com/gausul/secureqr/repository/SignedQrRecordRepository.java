package com.gausul.secureqr.repository;


import com.gausul.secureqr.model.SignedQrRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignedQrRecordRepository extends JpaRepository<SignedQrRecord, String> {
    // custom queries if needed
}
