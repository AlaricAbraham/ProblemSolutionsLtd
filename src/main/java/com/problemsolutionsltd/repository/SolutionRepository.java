package com.problemsolutionsltd.repository;

import com.problemsolutionsltd.model.Solution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolutionRepository
        extends JpaRepository<Solution, Long> {
}
