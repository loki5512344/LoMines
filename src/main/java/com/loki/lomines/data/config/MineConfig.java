package com.loki.lomines.data.config;

import org.bukkit.Location;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable configuration data model for a mine.
 * All fields are final to ensure immutability.
 * Use the Builder pattern to create instances.
 */
public final class MineConfig {

    // Regions
    private final List<Location> selections;

    // Blocks
    private final Map<String, Double> blocks;

    /** How blocks are placed on reset. */
    private final FillMode fillMode;
    /** Vanilla material name for {@link FillMode#MASK} scan (e.g. pink_concrete). */
    private final String maskMarkerMaterial;
    /** Block centers for mask mode; ignored when {@link #fillMode} is {@link FillMode#CUBOID}. */
    private final List<Location> maskPositions;

    // Rewards
    private final List<Reward> rewards;

    // Reset
    private final int resetTicks;
    private final double resetPercent;
    private final boolean resetOnPercentEnabled;
    private final List<String> resetCommands;
    private final String broadcastReset;

    // Teleportation
    private final boolean teleportOnReset;
    private final Location teleportLocation;

    // Action Bar
    private final boolean actionBarEnabled;
    private final String actionBarMessage;
    private final double actionBarRange;

    // Timer
    private final String timerFormat;

    MineConfig(List<Location> selections, Map<String, Double> blocks, FillMode fillMode,
               String maskMarkerMaterial, List<Location> maskPositions, List<Reward> rewards,
               int resetTicks, double resetPercent, boolean resetOnPercentEnabled,
               List<String> resetCommands, String broadcastReset, boolean teleportOnReset,
               Location teleportLocation, boolean actionBarEnabled, String actionBarMessage,
               double actionBarRange, String timerFormat) {
        this.selections = List.copyOf(selections);
        this.blocks = Map.copyOf(blocks);
        this.fillMode = fillMode;
        this.maskMarkerMaterial = maskMarkerMaterial;
        this.maskPositions = List.copyOf(maskPositions);
        this.rewards = List.copyOf(rewards);
        this.resetTicks = resetTicks;
        this.resetPercent = resetPercent;
        this.resetOnPercentEnabled = resetOnPercentEnabled;
        this.resetCommands = List.copyOf(resetCommands);
        this.broadcastReset = broadcastReset;
        this.teleportOnReset = teleportOnReset;
        this.teleportLocation = teleportLocation;
        this.actionBarEnabled = actionBarEnabled;
        this.actionBarMessage = actionBarMessage;
        this.actionBarRange = actionBarRange;
        this.timerFormat = timerFormat;
    }

    public static MineConfigBuilder builder() {
        return new MineConfigBuilder();
    }
    
    // Getters
    public List<Location> getSelections() {
        return selections;
    }
    
    public Map<String, Double> getBlocks() {
        return blocks;
    }
    
    public FillMode getFillMode() {
        return fillMode;
    }
    
    /**
     * Material name for scanning marker blocks (vanilla), e.g. {@code pink_concrete}.
     */
    public String getMaskMarkerMaterial() {
        return maskMarkerMaterial;
    }
    
    public List<Location> getMaskPositions() {
        return maskPositions;
    }
    
    public List<Reward> getRewards() {
        return rewards;
    }
    
    public int getResetTicks() {
        return resetTicks;
    }
    
    public double getResetPercent() {
        return resetPercent;
    }
    
    public boolean isResetOnPercentEnabled() {
        return resetOnPercentEnabled;
    }
    
    public List<String> getResetCommands() {
        return resetCommands;
    }
    
    public String getBroadcastReset() {
        return broadcastReset;
    }
    
    public boolean isTeleportOnReset() {
        return teleportOnReset;
    }
    
    public Location getTeleportLocation() {
        return teleportLocation;
    }
    
    public boolean isActionBarEnabled() {
        return actionBarEnabled;
    }
    
    public String getActionBarMessage() {
        return actionBarMessage;
    }
    
    public double getActionBarRange() {
        return actionBarRange;
    }
    
    public String getTimerFormat() {
        return timerFormat;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MineConfig that = (MineConfig) o;
        return resetTicks == that.resetTicks &&
                Double.compare(that.resetPercent, resetPercent) == 0 &&
                resetOnPercentEnabled == that.resetOnPercentEnabled &&
                teleportOnReset == that.teleportOnReset &&
                actionBarEnabled == that.actionBarEnabled &&
                Double.compare(that.actionBarRange, actionBarRange) == 0 &&
                Objects.equals(selections, that.selections) &&
                Objects.equals(blocks, that.blocks) &&
                fillMode == that.fillMode &&
                Objects.equals(maskMarkerMaterial, that.maskMarkerMaterial) &&
                Objects.equals(maskPositions, that.maskPositions) &&
                Objects.equals(rewards, that.rewards) &&
                Objects.equals(resetCommands, that.resetCommands) &&
                Objects.equals(broadcastReset, that.broadcastReset) &&
                Objects.equals(teleportLocation, that.teleportLocation) &&
                Objects.equals(actionBarMessage, that.actionBarMessage) &&
                Objects.equals(timerFormat, that.timerFormat);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(selections, blocks, fillMode, maskMarkerMaterial, maskPositions, rewards, resetTicks, resetPercent,
                resetOnPercentEnabled, resetCommands, broadcastReset, teleportOnReset,
                teleportLocation, actionBarEnabled, actionBarMessage, actionBarRange, timerFormat);
    }
}
