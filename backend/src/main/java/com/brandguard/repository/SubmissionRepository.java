package com.brandguard.repository;

import com.brandguard.domain.Submission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, String> {
    List<Submission> findAllByOrderByUpdatedAtDesc();
    long countByStatus(String status);
}
