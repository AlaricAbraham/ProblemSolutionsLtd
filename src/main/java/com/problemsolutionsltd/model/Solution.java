package com.problemsolutionsltd.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "solutions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Solution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 1000)
    private String description;

    @NotNull(message = "Category is required")
    @Enumerated(EnumType.STRING)
    private Category category;

    @Min(value = 0, message = "Stock cannot be negative")
    private int stockQuantity;

    @Min(value = 0, message = "Threshold cannot be negative")
    @Builder.Default
    private int reorderThreshold = 10; // Default warning level for new items

    @Min(value = 0, message = "Price cannot be negative")
    @NotNull(message = "Price is required")
    private BigDecimal price;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Status status = Status.AVAILABLE;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
