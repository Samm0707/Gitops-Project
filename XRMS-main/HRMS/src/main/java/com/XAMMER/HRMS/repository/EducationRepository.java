package com.XAMMER.HRMS.repository;

import com.XAMMER.HRMS.model.Education;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EducationRepository extends JpaRepository<Education, Long> {
    // Custom query methods can be added here if needed
}