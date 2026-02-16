package com.problemsolutionsltd.inventory.Entity;

public enum Status {
    AVAILABLE,      // Ready to ship
    OUT_OF_STOCK,   // We need to order more
    DISCONTINUED,   // Old tech (e.g., "Laser v1")
    RECALLED        // Whoops...
}
