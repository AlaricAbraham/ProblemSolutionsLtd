package com.problemsolutionsltd.inventory.repository;

import com.problemsolutionsltd.inventory.Entity.Solution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolutionRepository
        extends JpaRepository<Solution, Long> {
}
