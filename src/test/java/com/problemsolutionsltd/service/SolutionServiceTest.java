package com.problemsolutionsltd.service;

import com.problemsolutionsltd.model.Category;
import com.problemsolutionsltd.model.Solution;
import com.problemsolutionsltd.model.Status;
import com.problemsolutionsltd.repository.SolutionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Tells JUnit to activate Mockito
class SolutionServiceTest {

    @Mock // Create a fake database repository
    private SolutionRepository repository;

    @InjectMocks // Inject the fake database into our real Service
    private SolutionService service;

    @Test
    void createSolution_ThrowsException_WhenAvailableWithZeroStock() {
        // 1. Set up the bad data as a user might send it
        Solution badItem = Solution.builder()
                .name("Invisible Tripwire")
                .category(Category.NON_LETHAL)
                .stockQuantity(0)
                .price(new BigDecimal("500.00"))
                .status(Status.AVAILABLE)
                .build();

        // 2. Verify that calling the service throws our exact Exception
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            service.createSolution(badItem);
        });

        // 3. Check that the error message and status code are correct
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Invalid entry: An item with 0 stock cannot be set to ACTIVE. " +
                "Please enter a valid stock amount or change the status."));

        // 4. Verify the database save() method was NEVER called
        verify(repository, never()).save(any(Solution.class));
    }

    @Test
    void createSolution_SetsOutOfStock_WhenStatusNullAndStockZero() {
        // Force the status to be null to simulate a missing field
        Solution item = Solution.builder()
                .name("Empty Box")
                .stockQuantity(0)
                .status(null)
                .build();

        when(repository.save(any(Solution.class))).thenAnswer(i -> i.getArgument(0));

        Solution savedItem = service.createSolution(item);

        assertEquals(Status.OUT_OF_STOCK, savedItem.getStatus());
        verify(repository, times(1)).save(item);
    }

    @Test
    void createSolution_SetsActive_WhenStatusNullAndStockPositive() {
        // Force the status to be null to simulate a missing field
        Solution item = Solution.builder()
                .name("Laser Ammo")
                .stockQuantity(50)
                .status(null)
                .build();

        when(repository.save(any(Solution.class))).thenAnswer(i -> i.getArgument(0));

        Solution savedItem = service.createSolution(item);

        assertEquals(Status.AVAILABLE, savedItem.getStatus());
    }

    @Test
    void createSolution_PreservesStatus_WhenExplicitlySet() {
        // Manager explicitly discontinues an item that still has stock
        Solution item = Solution.builder()
                .name("Old Jetpack")
                .stockQuantity(10)
                .status(Status.DISCONTINUED)
                .build();
        when(repository.save(any(Solution.class))).thenAnswer(i -> i.getArgument(0));

        Solution savedItem = service.createSolution(item);

        // We should respect the manager and do nothing
        assertEquals(Status.DISCONTINUED, savedItem.getStatus());
    }

    @Test
    void getSolutionById_ReturnsItem_WhenIdExists() {
        // ARRANGE
        Solution item = Solution.builder().id(1L).name("Grappling Hook").build();
        // Tell fake DB: "When asked for ID 1, return this specific Optional"
        when(repository.findById(1L)).thenReturn(java.util.Optional.of(item));

        // ACT
        Solution foundItem = service.getSolutionById(1L);

        // ASSERT
        assertEquals("Grappling Hook", foundItem.getName());
    }

    @Test
    void getSolutionById_ThrowsException_WhenIdDoesNotExist() {
        // ARRANGE: Tell fake DB: "When asked for ID 99, return empty"
        when(repository.findById(99L)).thenReturn(java.util.Optional.empty());

        // ACT & ASSERT
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            service.getSolutionById(99L);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Asset not found"));
    }
}