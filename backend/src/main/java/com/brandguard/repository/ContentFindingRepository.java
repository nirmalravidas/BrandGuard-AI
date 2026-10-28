package com.brandguard.repository;

import com.brandguard.domain.ContentFinding;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentFindingRepository extends JpaRepository<ContentFinding, String> {
    List<ContentFinding> findBySubmissionId(String submissionId);
    void deleteBySubmissionId(String submissionId);
}
