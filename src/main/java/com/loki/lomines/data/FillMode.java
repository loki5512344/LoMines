package com.loki.lomines.data;

/**
 * How mine contents are placed on reset.
 * <ul>
 *   <li>{@link #CUBOID} — fill entire selection box (classic)</li>
 *   <li>{@link #MASK} — only at stored block positions (painted with a marker block, then scanned)</li>
 * </ul>
 */
public enum FillMode {
    CUBOID,
    MASK
}
