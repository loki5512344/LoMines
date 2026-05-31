package dev.loki.lomines.wand;

import org.bukkit.Location;

import java.util.Arrays;

/**
 * One player's progress: up to 9 sub-mines sharing one basename and one shared YAML template,
 * each slot has its own cuboid (pos1 + pos2).
 */
public final class GroupWandSession {

    private static final int SLOTS = 9;
    private final Location[] pos1 = new Location[SLOTS];
    private final Location[] pos2 = new Location[SLOTS];
    private String baseName = "";
    private int activeSlot;

    public String getBaseName() {
        return baseName;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName != null ? baseName.trim() : "";
    }

    public boolean hasBaseName() {
        return baseName != null && !baseName.isEmpty();
    }

    public int getActiveSlot() {
        return activeSlot;
    }

    public void setActiveSlot(int activeSlot) {
        if (activeSlot < 0 || activeSlot >= SLOTS) {
            throw new IllegalArgumentException("activeSlot 0..8");
        }
        this.activeSlot = activeSlot;
    }

    public Location getPos1(int index) {
        return pos1[index];
    }

    public Location getPos2(int index) {
        return pos2[index];
    }

    public void setPos1(int index, Location loc) {
        pos1[index] = loc;
    }

    public void setPos2(int index, Location loc) {
        pos2[index] = loc;
    }

    public boolean isSlotReady(int index) {
        return pos1[index] != null && pos2[index] != null;
    }

    public void clearCorners(int index) {
        pos1[index] = null;
        pos2[index] = null;
    }

    public void clearAll() {
        Arrays.fill(pos1, null);
        Arrays.fill(pos2, null);
    }

    public int countReadySlots() {
        int n = 0;
        for (int i = 0; i < SLOTS; i++) {
            if (isSlotReady(i)) {
                n++;
            }
        }
        return n;
    }
}
