package dev.loki.lomines.data.config.block;

/**
 * Strategy for placing blocks when resetting a mine.
 */
public enum FillMode {
    /**
     * Fill entire cuboid region with blocks.
     */
    CUBOID,

    /**
     * Fill only specific positions marked by a block type.
     * Allows custom shapes (spheres, toruses, etc.).
     */
    MASK
}
