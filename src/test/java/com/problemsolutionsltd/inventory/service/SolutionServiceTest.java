package com.problemsolutionsltd.inventory.service;

import com.problemsolutionsltd.inventory.Entity.Category;
import com.problemsolutionsltd.inventory.Entity.Solution;
import com.problemsolutionsltd.inventory.Entity.Status;
import com.problemsolutionsltd.inventory.repository.SolutionRepository;
import com.problemsolutionsltd.inventory.service.SolutionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Tells JUnit to activate Mockito
class SolutionServiceTest {

    @Mock // Create a fake database repository
    private SolutionRepository repository;

    @InjectMocks // Inject the fake database into our real Service
    private SolutionService service;



    // ==========================================
    // GET TESTS
    // ==========================================

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


    // ==========================================
    // POST TESTS
    // ==========================================
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


    // ==========================================
    // UPDATE (PUT) TESTS
    // ==========================================

    @Test
    void updateSolution_UpdatesFieldsSuccessfully_WhenValid() {
        // ARRANGE
        Solution existing = Solution.builder().id(1L).name("Old Laser").stockQuantity(5).status(Status.AVAILABLE).build();
        Solution updatedInfo = Solution.builder().name("New Laser").stockQuantity(15).status(Status.AVAILABLE).build();

        when(repository.findById(1L)).thenReturn(java.util.Optional.of(existing));
        when(repository.save(any(Solution.class))).thenAnswer(i -> i.getArgument(0));

        // ACT
        Solution result = service.updateSolution(1L, updatedInfo);

        // ASSERT
        assertEquals("New Laser", result.getName());
        assertEquals(15, result.getStockQuantity());
        assertEquals(Status.AVAILABLE, result.getStatus());
        verify(repository, times(1)).save(existing);
    }

    @Test
    void updateSolution_ThrowsException_WhenIdDoesNotExist() {
        // ARRANGE
        Solution updatedInfo = Solution.builder().name("Ghost Item").build();
        when(repository.findById(99L)).thenReturn(java.util.Optional.empty());

        // ACT & ASSERT
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            service.updateSolution(99L, updatedInfo);
        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(repository, never()).save(any(Solution.class));
    }

    @Test
    void updateSolution_ThrowsException_WhenSettingAvailableWithZeroStock() {
        // ARRANGE
        Solution existing = Solution.builder().id(1L).name("Box").stockQuantity(5).status(Status.AVAILABLE).build();
        // User maliciously tries to update stock to 0 but force status to AVAILABLE
        Solution updatedInfo = Solution.builder().name("Box").stockQuantity(0).status(Status.AVAILABLE).build();

        when(repository.findById(1L)).thenReturn(java.util.Optional.of(existing));

        // ACT & ASSERT
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            service.updateSolution(1L, updatedInfo);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(repository, never()).save(any(Solution.class));
    }

    @Test
    void updateSolution_AutoTogglesToOutOfStock_WhenStockDropsToZero() {
        // ARRANGE: Item was AVAILABLE with 5 stock
        Solution existing = Solution.builder().id(1L).name("Box").stockQuantity(5).status(Status.AVAILABLE).build();
        // User updates stock to 0, but forgets to provide a status
        Solution updatedInfo = Solution.builder().name("Box").stockQuantity(0).status(null).build();

        when(repository.findById(1L)).thenReturn(java.util.Optional.of(existing));
        when(repository.save(any(Solution.class))).thenAnswer(i -> i.getArgument(0));

        // ACT
        Solution result = service.updateSolution(1L, updatedInfo);

        // ASSERT: Brain should step in and toggle to OUT_OF_STOCK
        assertEquals(Status.OUT_OF_STOCK, result.getStatus());
    }

    @Test
    void updateSolution_AutoTogglesToAvailable_WhenStockRestored() {
        // ARRANGE: Item was OUT_OF_STOCK with 0 stock
        Solution existing = Solution.builder().id(1L).name("Box").stockQuantity(0).status(Status.OUT_OF_STOCK).build();
        // User receives a shipment of 10, but forgets to provide a status
        Solution updatedInfo = Solution.builder().name("Box").stockQuantity(10).status(null).build();

        when(repository.findById(1L)).thenReturn(java.util.Optional.of(existing));
        when(repository.save(any(Solution.class))).thenAnswer(i -> i.getArgument(0));

        // ACT
        Solution result = service.updateSolution(1L, updatedInfo);

        // ASSERT: Brain should step in and toggle to AVAILABLE
        assertEquals(Status.AVAILABLE, result.getStatus());
    }

    // ==========================================
    // DELETE TESTS
    // ==========================================

    @Test
    void deleteSolution_CallsRepository_WhenIdExists() {
        // ARRANGE
        when(repository.existsById(1L)).thenReturn(true);

        // ACT
        service.deleteSolution(1L);

        // ASSERT
        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void deleteSolution_ThrowsException_WhenIdDoesNotExist() {
        // ARRANGE
        when(repository.existsById(99L)).thenReturn(false);

        // ACT & ASSERT
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            service.deleteSolution(99L);
        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(repository, never()).deleteById(anyLong());
    }

    // ==========================================
    // CUSTOM QUERY TESTS
    // ==========================================

    @Test
    void getLowStockItems_ReturnsListOfItems_WhenCalled() {
        // ARRANGE
        Solution item = Solution.builder().name("Ammo").stockQuantity(1).reorderThreshold(5).build();
        // Mockito's List.of() creates a quick list with our one item
        when(repository.findItemsNeedingReorder()).thenReturn(List.of(item));

        // ACT
        List<Solution> result = service.getLowStockItems();

        // ASSERT
        assertEquals(1, result.size());
        assertEquals("Low Ammo", result.get(0).getName());
        verify(repository, times(1)).findItemsNeedingReorder();
    }
}