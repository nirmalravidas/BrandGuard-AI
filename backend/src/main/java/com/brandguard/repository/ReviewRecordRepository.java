package com.brandguard.repository;

import com.brandguard.domain.ReviewRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRecordRepository extends JpaRepository<ReviewRecord, String> {
    List<ReviewRecord> findBySubmissionIdOrderByCreatedAtAsc(String submissionId);
}
