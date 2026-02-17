package com.problemsolutionsltd.inventory.repository;

import com.problemsolutionsltd.inventory.Entity.Solution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolutionRepository
        extends JpaRepository<Solution, Long> {
    @Query("SELECT s FROM Solution s WHERE s.stockQuantity <= s.reorderThreshold")
    List<Solution> findItemsNeedingReorder();
}
