package com.loki.lomines.data.config;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
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
    
    private MineConfig(Builder builder) {
        this.selections = List.copyOf(builder.selections);
        this.blocks = Map.copyOf(builder.blocks);
        this.fillMode = builder.fillMode;
        this.maskMarkerMaterial = builder.maskMarkerMaterial;
        this.maskPositions = List.copyOf(builder.maskPositions);
        this.rewards = List.copyOf(builder.rewards);
        this.resetTicks = builder.resetTicks;
        this.resetPercent = builder.resetPercent;
        this.resetOnPercentEnabled = builder.resetOnPercentEnabled;
        this.resetCommands = List.copyOf(builder.resetCommands);
        this.broadcastReset = builder.broadcastReset;
        this.teleportOnReset = builder.teleportOnReset;
        this.teleportLocation = builder.teleportLocation;
        this.actionBarEnabled = builder.actionBarEnabled;
        this.actionBarMessage = builder.actionBarMessage;
        this.actionBarRange = builder.actionBarRange;
        this.timerFormat = builder.timerFormat;
    }
    
    public static Builder builder() {
        return new Builder();
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
    
    public static final class Builder {
        private List<Location> selections = new ArrayList<>();
        private Map<String, Double> blocks = new HashMap<>();
        private FillMode fillMode = FillMode.CUBOID;
        private String maskMarkerMaterial = "pink_concrete";
        private List<Location> maskPositions = new ArrayList<>();
        private List<Reward> rewards = new ArrayList<>();
        private int resetTicks = 6000;
        private double resetPercent = 10.0;
        private boolean resetOnPercentEnabled = false;
        private List<String> resetCommands = new ArrayList<>();
        private String broadcastReset = "";
        private boolean teleportOnReset = false;
        private Location teleportLocation = null;
        private boolean actionBarEnabled = true;
        private String actionBarMessage = "";
        private double actionBarRange = 50.0;
        private String timerFormat = "mm:ss";
        
        private Builder() {
        }
        
        public Builder selections(List<Location> selections) {
            this.selections = new ArrayList<>(selections);
            return this;
        }
        
        public Builder blocks(Map<String, Double> blocks) {
            this.blocks = new HashMap<>(blocks);
            return this;
        }
        
        public Builder fillMode(FillMode fillMode) {
            this.fillMode = fillMode != null ? fillMode : FillMode.CUBOID;
            return this;
        }
        
        public Builder maskMarkerMaterial(String maskMarkerMaterial) {
            this.maskMarkerMaterial = maskMarkerMaterial != null ? maskMarkerMaterial : "pink_concrete";
            return this;
        }
        
        public Builder maskPositions(List<Location> maskPositions) {
            this.maskPositions = maskPositions != null ? new ArrayList<>(maskPositions) : new ArrayList<>();
            return this;
        }
        
        public Builder rewards(List<Reward> rewards) {
            this.rewards = new ArrayList<>(rewards);
            return this;
        }
        
        public Builder resetTicks(int resetTicks) {
            this.resetTicks = resetTicks;
            return this;
        }
        
        public Builder resetPercent(double resetPercent) {
            this.resetPercent = resetPercent;
            return this;
        }
        
        public Builder resetOnPercentEnabled(boolean resetOnPercentEnabled) {
            this.resetOnPercentEnabled = resetOnPercentEnabled;
            return this;
        }
        
        public Builder resetCommands(List<String> resetCommands) {
            this.resetCommands = new ArrayList<>(resetCommands);
            return this;
        }
        
        public Builder broadcastReset(String broadcastReset) {
            this.broadcastReset = broadcastReset;
            return this;
        }
        
        public Builder teleportOnReset(boolean teleportOnReset) {
            this.teleportOnReset = teleportOnReset;
            return this;
        }
        
        public Builder teleportLocation(Location teleportLocation) {
            this.teleportLocation = teleportLocation;
            return this;
        }
        
        public Builder actionBarEnabled(boolean actionBarEnabled) {
            this.actionBarEnabled = actionBarEnabled;
            return this;
        }
        
        public Builder actionBarMessage(String actionBarMessage) {
            this.actionBarMessage = actionBarMessage;
            return this;
        }
        
        public Builder actionBarRange(double actionBarRange) {
            this.actionBarRange = actionBarRange;
            return this;
        }
        
        public Builder timerFormat(String timerFormat) {
            this.timerFormat = timerFormat;
            return this;
        }
        
        public MineConfig build() {
            return new MineConfig(this);
        }
    }
}
