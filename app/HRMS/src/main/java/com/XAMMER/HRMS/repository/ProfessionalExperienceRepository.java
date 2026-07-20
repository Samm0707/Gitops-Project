package com.XAMMER.HRMS.repository;

import com.XAMMER.HRMS.model.ProfessionalExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfessionalExperienceRepository extends JpaRepository<ProfessionalExperience, Long> {
    // Spring Data JPA automatically provides standard CRUD operations (save, findById, findAll, delete, etc.)
    // You can add custom query methods here if needed in the future, e.g.:
    // List<ProfessionalExperience> findByUserUsername(String username);
}