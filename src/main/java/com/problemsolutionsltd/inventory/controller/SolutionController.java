package com.problemsolutionsltd.inventory.controller;

import com.problemsolutionsltd.inventory.Entity.Solution;
import com.problemsolutionsltd.inventory.service.SolutionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/solutions")
public class SolutionController {

    private final SolutionService service;

    public SolutionController(SolutionService service) {
        this.service = service;
    }

    // GET: http://localhost:8080/api/v1/solutions
    @GetMapping
    public List<Solution> getAllSolutions() {
        return service.getAllSolutions();
    }

    // GET: http://localhost:8080/api/v1/solutions/{id}
    @GetMapping("/{id}")
    public Solution getSolutionById(@PathVariable Long id) {
        return service.getSolutionById(id);
    }

    // POST: http://localhost:8080/api/v1/solutions
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // Return 201 instead of 200
    public Solution createSolution(
            @Valid @RequestBody Solution newSolution
    ) {
        return service.createSolution(newSolution);
    }
}
