package com.brandguard.repository;

import com.brandguard.domain.ContentRevision;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentRevisionRepository extends JpaRepository<ContentRevision, String> {
    List<ContentRevision> findBySubmissionIdOrderByCreatedAtAsc(String submissionId);
}
