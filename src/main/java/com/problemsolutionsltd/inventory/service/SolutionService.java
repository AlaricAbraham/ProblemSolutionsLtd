package com.problemsolutionsltd.inventory.service;

import com.problemsolutionsltd.inventory.Entity.Solution;
import com.problemsolutionsltd.inventory.Entity.Status;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import com.problemsolutionsltd.inventory.repository.SolutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException; // to implement in phase 2 REMOVE COMMENT BEFORE COMMIT

import java.util.List;

@Service
public class SolutionService {

    private final SolutionRepository repository;

    public SolutionService(SolutionRepository repository) {
        this.repository = repository;
    }

    // --- Business Methods ---

    public List<Solution> getAllSolutions() {
        // fetch everything
        return repository.findAll();
    }

    public Solution createSolution(Solution newSolution) {

        // Scenario 1: The manager forgot to set a status. Let's be helpful.
        if (newSolution.getStatus() == null) {
            if (newSolution.getStockQuantity() == 0) {
                newSolution.setStatus(Status.OUT_OF_STOCK);
            } else {
                newSolution.setStatus(Status.AVAILABLE);
            }
        }
        // Scenario 2: The manager explicitly set it to ACTIVE, but stock is 0.
        // We intervene because we can't sell nothing. Deny the request and prompt them to fix it.
        else if (newSolution.getStatus() == Status.AVAILABLE && newSolution.getStockQuantity() == 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid entry: An item with 0 stock cannot be set to ACTIVE. " +
                            "Please enter a valid stock amount or change the status."
            );
        }

        // Scenario 3: The manager explicitly set DISCONTINUED or RECALLED.
        // We do nothing. We respect their input completely.

        // Save to the database and return the saved item
        return repository.save(newSolution);
    }

    public Solution getSolutionById(Long id) {
        // We use an Optional here. If the ID exists, return it.
        // If not, throw a 404 Not Found exception.
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Asset not found. It may have been confiscated."
                ));
    }

    public Solution updateSolution(Long id, @Valid Solution updatedSolution) {
        // First, verify the solution exists
        Solution existingSolution = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Asset not found. It may have been confiscated."
                ));

        // Apply the same business logic as createSolution
        if (updatedSolution.getStatus() == null) {
            if (updatedSolution.getStockQuantity() == 0) {
                updatedSolution.setStatus(Status.OUT_OF_STOCK);
            } else {
                updatedSolution.setStatus(Status.AVAILABLE);
            }
        } else if (updatedSolution.getStatus() == Status.AVAILABLE && updatedSolution.getStockQuantity() == 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid entry: An item with 0 stock cannot be set to ACTIVE. " +
                            "Please enter a valid stock amount or change the status."
            );
        }

        // Update all fields
        existingSolution.setName(updatedSolution.getName());
        existingSolution.setDescription(updatedSolution.getDescription());
        existingSolution.setCategory(updatedSolution.getCategory());
        existingSolution.setStockQuantity(updatedSolution.getStockQuantity());
        existingSolution.setReorderThreshold(updatedSolution.getReorderThreshold());
        existingSolution.setPrice(updatedSolution.getPrice());
        existingSolution.setStatus(updatedSolution.getStatus());

        // Save and return
        return repository.save(existingSolution);
    }

    public void deleteSolution(Long id) {
        // Make sure it exists first, otherwise throw a 404
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Asset not found. It may have already been destroyed."
            );
        }
        repository.deleteById(id);
    }
}
